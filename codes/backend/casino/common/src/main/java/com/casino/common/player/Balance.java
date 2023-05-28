package com.casino.common.player;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Balance {
    private final BigDecimal initialBalance;
    private volatile BigDecimal current;

    public Balance(BigDecimal initialBalance) {
        super();
        this.current = initialBalance;
        this.initialBalance = initialBalance;
    }

    public synchronized void updateBalance(BigDecimal amount) {
        this.current = amount;
    }

    public synchronized void add(BigDecimal addAmount) {
        current = current.add(addAmount);
    }

    public synchronized void subtract(BigDecimal subtractAmount) {
        current = current.subtract(subtractAmount);
    }

    public BigDecimal getCurrent() {
        return current.setScale(2, RoundingMode.DOWN);
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

}
