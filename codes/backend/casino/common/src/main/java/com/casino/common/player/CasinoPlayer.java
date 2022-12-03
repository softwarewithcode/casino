package com.casino.common.player;

import java.math.BigDecimal;
import java.util.ConcurrentModificationException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import com.casino.common.bet.BetUtil;
import com.casino.common.common.Result;
import com.casino.common.exception.IllegalBetException;
import com.casino.common.table.ICasinoTable;

public abstract class CasinoPlayer implements ICasinoPlayer {

	private final String name;
	private final UUID id;
	private final BigDecimal initialBalance;
	private BigDecimal endBalance;
	private BigDecimal balance;
	private BigDecimal bet;
	private Status status;
	private ICasinoTable table;
	private final ReentrantLock balanceLock;

	public CasinoPlayer(String name, UUID id, BigDecimal initialBalance, ICasinoTable table) {
		super();
		this.name = name;
		this.id = id;
		this.initialBalance = initialBalance;
		this.balance = initialBalance;
		this.status = null;
		this.table = table;
		this.balanceLock = new ReentrantLock(true); // !?
	}

	@Override
	public String getName() {
		return name;
	}

	public ICasinoTable getTable() {
		return table;
	}

	@Override
	public BigDecimal getInitialBalance() {
		return initialBalance;
	}

	@Override
	public BigDecimal getEndBalance() {
		return endBalance;
	}

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public void onLeave() {
		// TODO Auto-generated method stub

	}

	public ReentrantLock getBalanceLock() {
		return balanceLock;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CasinoPlayer other = (CasinoPlayer) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public Status getStatus() {
		return status;
	}

	@Override
	public void setStatus(Status status) {
		// TODO Auto-generated method stub

	}

	@Override
	public BigDecimal getBet() {
		return bet;
	}

	@Override
	public void updateBet(BigDecimal bet, ICasinoTable table) {
		BetUtil.verifyBet(table, this, bet);
		this.bet = bet;
	}

	public void increaseBet(BigDecimal increaseAmount) {
		BetUtil.verifySufficentBalance(increaseAmount, this);
		if (this.bet == null)
			throw new IllegalBetException("increase called but no initial bet was found", 6);
		this.balance = this.getBalance().subtract(increaseAmount);
		this.bet = getBet().add(increaseAmount);
	}

	@Override
	public void calculateBalance(Result result) {
		// TODO Auto-generated method stub

	}

	@Override
	public BigDecimal getBalance() {
		return balance;
	}

	@Override
	public void clearBet() {
		this.bet = null;
	}

	@Override
	public void updateBalance(BigDecimal balance) {
		try {
			if (balance == null || balance.compareTo(BigDecimal.ZERO) < 0)
				throw new IllegalArgumentException("Balance missing or negative:" + balance);
			if (!balanceLock.tryLock())
				throw new ConcurrentModificationException("balance lock was not obtained");
			this.balance = balance;
		} finally {
			if (balanceLock.isHeldByCurrentThread())
				balanceLock.unlock();
		}
	}
}
