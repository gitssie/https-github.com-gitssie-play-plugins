package com.ulopay.settlement.process;


import com.google.common.collect.Lists;
import com.ulopay.settlement.iterator.RefundIterator;
import com.ulopay.settlement.iterator.ThridIterator;
import com.ulopay.settlement.iterator.TradeIterator;
import com.ulopay.settlement.models.Refund;
import com.ulopay.settlement.models.Trade;
import com.ulopay.settlement.models.TradeRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.F;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BatchProcess {
    private static final Logger LOGGER = LoggerFactory.getLogger(BatchProcess.class);

    private TradeIterator tradeIterator;
    private RefundIterator refundIterator;
    private ThridIterator thridIterator;
    private TradeRowHandler rowHandler;
    private TradeRowCollect rowCollect;
    private AtomicLong erroCount = new AtomicLong(0);
    private AtomicLong tradeCount = new AtomicLong(0);
    private AtomicLong refundCount = new AtomicLong(0);
    private AtomicLong thridCount = new AtomicLong(0);

    private Lock lock = new ReentrantLock();
    private AtomicBoolean isProcessRemaining = new AtomicBoolean(false);

    private List<Exception> exceptions = Lists.newArrayList();

    public void proccess(){
        proccessTrade();
        processRefund();
        processThridRemaining();
    }

    /**
     *  比对每一笔交易订单、退款单
     *  交易订单：查询数据库里当天的订单，状态为 交易成功+退款，以第三方为准
     *          1）数据库里有，第三方也有
     *                      对比状态，状态不一致则以第三方为准
     *                      对比金额，金额不一致，记录差错信息
     *          2）数据库里有，第三方没有
     *                      记录差错信息
     *          3）数据库里没有，第三方有
     *                      记录差错信息
     *  退款单：查询数据库里，提交的退款订单，以第三方为准
     *          1）数据库里有，第三方也有
     *                      对比状态，状态不一致则以第三方为准
     *                      对比金额，金额不一致，记录差错信息
     *          2）数据库里有，第三方没有
     *                      以第三方为准 （***这种第三方跨天了，然后本地商户不交易了，平台会亏损***）
     *                      交易接口要限制凌晨跨天时不能调用退款接口、冲正接口
     *          3）数据库里没有，第三方有
     *                      以第三方为准，第三方把冲正当成了退款
     */

    public void proccessTrade() {
        try{
            proccessTradeInner();
        }catch (Exception e){
            exceptions.add(e);
        }
    }

    public void proccessTradeInner() throws Exception {
        LOGGER.info("开始处理交易订单,交易订单信息:{0},对账单信息:{0}",tradeIterator,thridIterator);
        TradeRow tradeRow;
        Trade trade;
        F.Tuple<Integer,String> matchResult;
        while(tradeIterator.hasNext()){
            tradeCount.incrementAndGet();

            trade = tradeIterator.next();
            tradeRow = thridIterator.findTrade(trade);
            if(tradeRow == null){
                 LOGGER.warn("未从第三方账单找到本地交易:{0}",trade);
            }else{
                thridCount.incrementAndGet();
            }
            matchResult = rowHandler.matchTrade(trade,tradeRow);
            if(matchResult._1 != 0){ //非0表示对比错误
                erroCount.incrementAndGet();
                LOGGER.warn("交易订单对比异常,本地交易{0},账单交易{0},异常码:{0},异常信息:{0}",trade,tradeRow,matchResult._1,matchResult._2);
            }
            //汇总交易信息
            rowCollect.collect(trade,tradeRow,matchResult);

            thridIterator.remove(tradeRow);
        }
    }

    public void processRefund(){
        try{
            processRefundInner();
        }catch (Exception e){
            exceptions.add(e);
        }
    }

    public void processRefundInner() throws Exception {
        LOGGER.info("开始处理退款订单,退款订单信息:{0},对账单信息:{0}",refundIterator,thridIterator);
        TradeRow tradeRow;
        Refund refund;
        F.Tuple<Integer,String> matchResult;
        while(refundIterator.hasNext()){
            refundCount.incrementAndGet();

            refund = refundIterator.next();
            tradeRow = thridIterator.findRefund(refund);
            if(tradeRow == null){
                LOGGER.warn("未从第三方账单找到本地退款:{0}",refund);
            }else{
                thridCount.incrementAndGet();
            }
            matchResult = rowHandler.matchRefund(refund,tradeRow);
            if(matchResult._1 != 0){ //非0表示对比错误
                erroCount.incrementAndGet();
                LOGGER.warn("交易订单对比异常,本地退款{0},账单交易{0},异常码:{0},异常信息:{0}",refund,tradeRow,matchResult._1,matchResult._2);
            }
            //汇总交易信息
            rowCollect.collect(refund,tradeRow,matchResult);

            thridIterator.remove(tradeRow);
        }
    }

    public void processThridRemaining() {
        try{
            if(!isProcessRemaining.get()){
                boolean locked = lock.tryLock(5, TimeUnit.SECONDS);
                try{
                    if(locked && !isProcessRemaining.get()){
                        isProcessRemaining.set(true);
                        processThridRemainingInnder();
                    }
                }finally {
                    if (locked) {
                        lock.unlock();
                    }
                }
            }
        }catch (Exception e){
            exceptions.add(e);
        }
    }

    private void processThridRemainingInnder() throws Exception {
        Iterator<TradeRow> rowIterator = thridIterator.toIterator();
        TradeRow tradeRow;
        F.Tuple<Integer,String> matchResult;
        Refund refund;
        Trade trade;
        while(rowIterator.hasNext()){
            tradeRow = rowIterator.next();
            thridCount.incrementAndGet();

            if(tradeRow.isRefund()){
                refund = refundIterator.findRefund(tradeRow);
                matchResult = rowHandler.matchRefund(refund,tradeRow);
                if(matchResult._1 != 0){ //非0表示对比错误
                    erroCount.incrementAndGet();
                    LOGGER.warn("交易订单对比异常,本地退款{0},账单交易{0},异常码:{0},异常信息:{0}",refund,tradeRow,matchResult._1,matchResult._2);
                }
                if(refund != null){
                    refundCount.incrementAndGet();
                }
                //汇总交易信息
                rowCollect.collect(refund,tradeRow,matchResult);
            }else{
                trade = tradeIterator.findTrade(tradeRow);
                matchResult = rowHandler.matchTrade(trade,tradeRow);
                if(matchResult._1 != 0){ //非0表示对比错误
                    erroCount.incrementAndGet();
                    LOGGER.warn("交易订单对比异常,本地交易{0},账单交易{0},异常码:{0},异常信息:{0}",trade,tradeRow,matchResult._1,matchResult._2);
                }
                if(trade != null){
                    tradeCount.incrementAndGet();
                }
                //汇总交易信息
                rowCollect.collect(trade,tradeRow,matchResult);
            }
        }
    }
}
