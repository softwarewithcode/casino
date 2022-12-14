package com.casino.common.player;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ConcurrentModificationException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.common.bet.BetUtil;
import com.casino.common.exception.IllegalBetException;
import com.casino.common.table.ICasinoTable;
import com.casino.common.user.Bridge;

public abstract class CasinoPlayer implements ICasinoPlayer {
	private static final Logger LOGGER = Logger.getLogger(CasinoPlayer.class.getName());
	private final ICasinoTable table;
	private final ReentrantLock playerLock;
	private BigDecimal endBalance;
	private volatile BigDecimal balance;
	private volatile BigDecimal totalBet;
	private volatile Status status;
	private final Bridge bridge;

	public CasinoPlayer(Bridge bridge, ICasinoTable table) {
		super();
		this.bridge = bridge;
		this.balance = bridge.initialBalance();
		this.balance = this.balance.setScale(2, RoundingMode.DOWN);
		this.status = null;
		this.table = table;
		this.playerLock = new ReentrantLock();
	}

	@Override
	public String getName() {
		return bridge.name();
	}

	public ICasinoTable getTable() {
		return table;
	}

	@Override
	public BigDecimal getInitialBalance() {
		return bridge.initialBalance();
	}

	@Override
	public BigDecimal getEndBalance() {
		return endBalance;
	}

	@Override
	public UUID getId() {
		return bridge.playerId();
	}

	@Override
	public void onLeave() {
		// TODO Auto-generated method stub

	}

	protected ReentrantLock getPlayerLock() {
		return playerLock;
	}

	@Override
	public int hashCode() {
		return Objects.hash(bridge.playerId());
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
		return Objects.equals(bridge.playerId(), other.bridge.playerId());
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
	public <T> void sendMessage(T t) {
		if (bridge.session() == null || !bridge.session().isOpen()) {
//			System.out.println("Session not found from bridge -> " + getName() + " will not receive message ->" + t + " " + System.nanoTime() + " conductor:" + Thread.currentThread());
			return;
		}
		try {
			bridge.session().getBasicRemote().sendText("Table " + getId() + " " + t);
		} catch (IOException e) {
			UUID logIdentifier = UUID.randomUUID();
			LOGGER.log(Level.SEVERE, "Could not reach player: logIdentifier: " + logIdentifier + " name;" + getName() + " id:" + getId(), e);
			throw new RuntimeException("cannot be reached ->  LogId:" + logIdentifier);
		}
	}

	@Override
	public void updateStartingBet(BigDecimal bet, ICasinoTable table) {
		verifyCallersLock();
		BetUtil.verifyStartingBet(table, this, bet);
		this.totalBet = bet;
	}

	protected void updateBalanceAndBet(BigDecimal increaseAmount) {
		verifyCallersLock();
		BetUtil.verifySufficentBalance(increaseAmount, this);
		if (this.totalBet == null)
			throw new IllegalBetException("increase called but no initial bet was found", 6);
		this.balance = this.getBalance().subtract(increaseAmount);
		this.totalBet = getTotalBet().add(increaseAmount);
	}

	private void verifyCallersLock() {
		if (!playerLock.isHeldByCurrentThread())
			throw new IllegalBetException("lock is missing", 8);
	}

	@Override
	public void subtractTotalBetFromBalance() {
		try {
			tryTakingPlayerLock();
			if (balance == null || balance.compareTo(BigDecimal.ZERO) < 0)
				throw new IllegalArgumentException("Balance missing or negative. Waiting for manager's call" + balance);
			if (totalBet == null || totalBet.compareTo(BigDecimal.ZERO) < 0)
				throw new IllegalBetException("Bet missing or negative:" + totalBet, 9);
			this.balance = balance.subtract(totalBet);
		} finally {
			releasePlayerLock();
		}
	}

	protected void releasePlayerLock() {
		if (getPlayerLock().isHeldByCurrentThread())
			getPlayerLock().unlock();
	}

	protected void tryTakingPlayerLock() {
		if (!getPlayerLock().tryLock())
			throw new ConcurrentModificationException("playerLock was not obtained" + playerLock + " t:" + Thread.currentThread());
	}

	public void increaseBalance(BigDecimal amount) {
		try {
			tryTakingPlayerLock();
			this.balance = balance.add(amount);
		} finally {
			releasePlayerLock();
		}
	}

	@Override
	public void removeTotalBet() {
		try {
			tryTakingPlayerLock();
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
		return balance.setScale(2, RoundingMode.DOWN);
	}

	public Bridge getBridge() {
		return bridge;
	}

	@Override
	public void reset() {
		verifyCallersLock();
		this.totalBet = null;
	}

}
