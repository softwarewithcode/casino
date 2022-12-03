package com.casino.common.player;

import java.math.BigDecimal;
import java.util.ConcurrentModificationException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import com.casino.common.bet.BetUtil;
import com.casino.common.exception.IllegalBetException;
import com.casino.common.table.ICasinoTable;

public abstract class CasinoPlayer implements ICasinoPlayer {

	private final String name;
	private final UUID id;
	private final BigDecimal initialBalance;
	private final ICasinoTable table;
	private final ReentrantLock playerLock;
	private BigDecimal endBalance;
	private BigDecimal balance;
	private BigDecimal bet;
	private Status status;

	public CasinoPlayer(String name, UUID id, BigDecimal initialBalance, ICasinoTable table) {
		super();
		this.name = name;
		this.id = id;
		this.initialBalance = initialBalance;
		this.balance = initialBalance;
		this.status = null;
		this.table = table;
		this.playerLock = new ReentrantLock(true); // !?
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

	public ReentrantLock getPlayerLock() {
		return playerLock;
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
	public void updateStartingBet(BigDecimal bet, ICasinoTable table) {
		try {// player can change starting bet as long as it is GamePhase.BET
			if (!getPlayerLock().tryLock())
				throw new ConcurrentModificationException("playerLock was not obtained");
			BetUtil.verifyStartingBet(table, this, bet);
			this.bet = bet;
		} finally {
			if (getPlayerLock().isHeldByCurrentThread())
				getPlayerLock().unlock();
		}

	}

	protected void increaseBet(BigDecimal increaseAmount) {
		if (!playerLock.isHeldByCurrentThread())
			throw new IllegalBetException("lock is missing", 8);
		BetUtil.verifySufficentBalance(increaseAmount, this);
		if (this.bet == null)
			throw new IllegalBetException("increase called but no initial bet was found", 6);
		this.balance = this.getBalance().subtract(increaseAmount);
		this.bet = getBet().add(increaseAmount);
	}

	@Override
	public void deriveBalanceFromBet() {
		try {
			if (balance == null || balance.compareTo(BigDecimal.ZERO) < 0)
				throw new IllegalArgumentException("Balance missing or negative. Waiting for manager's call" + balance);
			if (bet == null || bet.compareTo(BigDecimal.ZERO) < 0)
				throw new IllegalBetException("Bet missing or negative:" + bet, 9);
			if (!playerLock.tryLock())
				throw new ConcurrentModificationException("balance lock was not obtained");
			this.balance = balance.subtract(bet);
		} finally {
			if (playerLock.isHeldByCurrentThread())
				playerLock.unlock();
		}

	}

	@Override
	public BigDecimal getBalance() {
		return balance;
	}

	@Override
	public void clearBet() {
		this.bet = null;
	}

}
