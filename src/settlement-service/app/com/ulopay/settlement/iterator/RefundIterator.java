package com.ulopay.settlement.iterator;

import com.ulopay.settlement.models.Refund;
import com.ulopay.settlement.models.TradeRow;

import java.util.Iterator;

public class RefundIterator implements Iterator<Refund>{

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public Refund next() {
        return null;
    }

    public Refund findRefund(TradeRow tradeRow) {
        return null;
    }
}
