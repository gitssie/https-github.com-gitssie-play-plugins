package com.ulopay.settlement.process;

import com.ulopay.settlement.models.Refund;
import com.ulopay.settlement.models.Trade;
import com.ulopay.settlement.models.TradeRow;
import play.libs.F;



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
public class TradeRowHandler {

    private final F.Tuple<Integer,String> error = new F.Tuple<Integer,String>(1,"Null");
    private final F.Tuple<Integer,String> success = new F.Tuple<Integer,String>(0,"Success");


    public F.Tuple<Integer,String> matchTrade(Trade trade, TradeRow row){
        if(trade == null && row == null){
            return error;
        }

        if(trade  == null){

        }else if(row == null){

        }else{

        }

        return null;
    }

    public F.Tuple<Integer,String>  matchRefund(Refund refund,TradeRow row){
        if(refund == null && row == null){
            return error;
        }

        if(refund  == null){

        }else if(row == null){

        }else{

        }

        return null;
    }
}
