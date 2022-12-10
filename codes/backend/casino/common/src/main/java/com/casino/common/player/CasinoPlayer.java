package com.casino.common.player;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
	private volatile BigDecimal balance;
	private volatile BigDecimal totalBet;
	private Status status;

	public CasinoPlayer(String name, UUID id, BigDecimal initialBalance, ICasinoTable table) {
		super();
		this.name = name;
		this.id = id;
		this.initialBalance = initialBalance;
		this.balance = initialBalance;
		this.balance = this.balance.setScale(2, RoundingMode.DOWN);
		this.status = null;
		this.table = table;
		this.playerLock = new ReentrantLock();
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
		return this.totalBet != null ? this.totalBet.setScale(2, RoundingMode.DOWN) : null;
	}

	@Override
	public void updateStartingBet(BigDecimal bet, ICasinoTable table) {
		BetUtil.verifyStartingBet(table, this, bet);
		this.totalBet = bet;
		this.getFirstHand().updateBet(totalBet);
	}

	protected void updateBalanceAndBet(BigDecimal increaseAmount) {
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
			if (!getPlayerLock().tryLock())
				throw new ConcurrentModificationException("playerLock was not obtained" + playerLock + " t:" + Thread.currentThread());
			if (balance == null || balance.compareTo(BigDecimal.ZERO) < 0)
				throw new IllegalArgumentException("Balance missing or negative. Waiting for manager's call" + balance);
			if (totalBet == null || totalBet.compareTo(BigDecimal.ZERO) < 0)
				throw new IllegalBetException("Bet missing or negative:" + totalBet, 9);
			this.balance = balance.subtract(totalBet);
		} finally {
			if (getPlayerLock().isHeldByCurrentThread())
				getPlayerLock().unlock();
		}
	}

	public void increaseBalance(BigDecimal amount) {
		try {
			if (!getPlayerLock().tryLock())
				throw new ConcurrentModificationException("playerLock was not obtained " + amount);
			this.balance = balance.add(amount);
			System.out.println("incrase:"+amount+" total:"+balance+ " player:"+this.getName());
		} finally {
			if (getPlayerLock().isHeldByCurrentThread())
				getPlayerLock().unlock();
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
		System.out.println("balance:"+balance+" "+getName());
		return balance.setScale(2, RoundingMode.DOWN);
	}

	@Override
	public void reset() {
		this.totalBet = null;
	}

}
