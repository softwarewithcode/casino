package com.casino.common.player;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.locks.ReentrantLock;

public class Balance {
	private final BigDecimal initialBalance;
	private volatile BigDecimal current;
	private final ReentrantLock balanceLock;

	public Balance(BigDecimal initialBalance) {
		super();
		this.current = initialBalance;
		this.initialBalance = initialBalance;
		balanceLock = new ReentrantLock(true);
	}

	public void updateBalance(BigDecimal amount) {
		try {
			balanceLock.lock();
			this.current = amount;
		} finally {
			balanceLock.unlock();
		}
	}

	public void add(BigDecimal addAmount) {
		try {
			balanceLock.lock();
			current = current.add(addAmount);
		} finally {
			balanceLock.unlock();
		}
	}

	public void subtract(BigDecimal subtractAmount) {
		try {
			balanceLock.lock();
			current = current.subtract(subtractAmount);
		} finally {
			balanceLock.unlock();
		}
	}

	public BigDecimal getCurrent() {
		return current.setScale(2, RoundingMode.DOWN);
	}

	public BigDecimal getInitialBalance() {
		return initialBalance;
	}

}
