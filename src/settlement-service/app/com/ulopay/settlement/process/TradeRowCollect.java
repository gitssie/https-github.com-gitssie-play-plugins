package com.ulopay.settlement.process;

import com.ulopay.settlement.models.Refund;
import com.ulopay.settlement.models.Trade;
import com.ulopay.settlement.models.TradeRow;
import play.libs.F;

public class TradeRowCollect {

    public void collect(Trade trade, TradeRow tradeRow, F.Tuple<Integer,String> matchResult){


    }

    public void collect(Refund refund, TradeRow tradeRow, F.Tuple<Integer,String> matchResult){


    }
}
