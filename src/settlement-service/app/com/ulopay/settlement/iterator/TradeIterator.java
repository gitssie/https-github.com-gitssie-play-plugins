package com.ulopay.settlement.iterator;

import com.ulopay.settlement.models.Trade;
import com.ulopay.settlement.models.TradeRow;

import java.util.Iterator;

public class TradeIterator implements Iterator<Trade>{

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public Trade next() {
        return null;
    }

    public Trade findTrade(TradeRow tradeRow) {
        return null;
    }
}
