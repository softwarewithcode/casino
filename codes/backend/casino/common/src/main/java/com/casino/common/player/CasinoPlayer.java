package com.casino.common.player;

import java.math.BigDecimal;
import java.util.ConcurrentModificationException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import com.casino.common.bet.BetUtil;
import com.casino.common.exception.IllegalBetException;
import com.casino.common.table.ICasinoTable;

public abstract class CasinoPlayer implements ICasinoPlayer {
	private static final Logger LOGGER = Logger.getLogger(CasinoPlayer.class.getName());
	private final String name;
	private final UUID id;
	private final BigDecimal initialBalance;
	private final ICasinoTable table;
	private final ReentrantLock playerLock;
	private BigDecimal endBalance;
	private BigDecimal balance;
	private BigDecimal totalBet;
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
	public BigDecimal getTotalBet() {
		return totalBet;
	}

	@Override
	public void updateTotalBet(BigDecimal bet, ICasinoTable table) {
		try {// player can change starting bet as long as it is GamePhase.BET
			if (!getPlayerLock().tryLock())
				throw new ConcurrentModificationException("playerLock was not obtained");
			BetUtil.verifyStartingBet(table, this, bet);
			this.totalBet = bet;
			System.out.println("TOTAL:" + bet);
		} finally {
			if (getPlayerLock().isHeldByCurrentThread())
				getPlayerLock().unlock();
		}

	}

	protected void increaseTotalBet(BigDecimal increaseAmount) {
		if (!playerLock.isHeldByCurrentThread())
			throw new IllegalBetException("lock is missing", 8);
		BetUtil.verifySufficentBalance(increaseAmount, this);
		if (this.totalBet == null)
			throw new IllegalBetException("increase called but no initial bet was found", 6);
		this.balance = this.getBalance().subtract(increaseAmount);
		this.totalBet = getTotalBet().add(increaseAmount);
	}

	@Override
	public void subtractTotalBetFromBalance() {
		try {
			if (balance == null || balance.compareTo(BigDecimal.ZERO) < 0)
				throw new IllegalArgumentException("Balance missing or negative. Waiting for manager's call" + balance);
			if (totalBet == null || totalBet.compareTo(BigDecimal.ZERO) < 0)
				throw new IllegalBetException("Bet missing or negative:" + totalBet, 9);
			if (!playerLock.tryLock())
				throw new ConcurrentModificationException("playerLock was not obtained");
			this.balance = balance.subtract(totalBet);
		} finally {
			if (playerLock.isHeldByCurrentThread())
				playerLock.unlock();
		}
	}

	@Override
	public void removeTotalBet() {
		try {
			if (!playerLock.tryLock())
				throw new ConcurrentModificationException("playerLock was not obtained");
			this.totalBet = null;
		} finally {
			if (playerLock.isHeldByCurrentThread())
				playerLock.unlock();
		}
	}

	public boolean hasBet() {
		return this.totalBet != null && totalBet.compareTo(BigDecimal.ZERO) == 1;
	}

	@Override
	public BigDecimal getBalance() {
		return balance;
	}

	@Override
	public void clearBet() {
		this.totalBet = null;
	}

}
