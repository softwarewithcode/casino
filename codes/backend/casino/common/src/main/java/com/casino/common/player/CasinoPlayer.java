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

import com.casino.common.bet.BetVerifier;
import com.casino.common.exception.IllegalBetException;
import com.casino.common.table.ICasinoTable;
import com.casino.common.user.Bridge;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(value = { "id", "bridge" })
public abstract class CasinoPlayer implements ICasinoPlayer {
	private static final Logger LOGGER = Logger.getLogger(CasinoPlayer.class.getName());
	private final ICasinoTable table;
	private final ReentrantLock playerLock;
	private final Bridge bridge;
	private BigDecimal endBalance;
	private BigDecimal initialBalance;
	private volatile BigDecimal balance;
	private volatile BigDecimal totalBet;
	private volatile BigDecimal payout;
	private volatile PlayerStatus status;
	private volatile Integer skippedBetRounds = 0;

	public CasinoPlayer(Bridge bridge, ICasinoTable table) {
		super();
		this.bridge = bridge;
		this.balance = bridge.initialBalance();
		this.initialBalance = bridge.initialBalance();
		this.balance = this.balance.setScale(2, RoundingMode.DOWN);
		this.status = null;
		this.payout = BigDecimal.ZERO;
		this.table = table;
		this.playerLock = new ReentrantLock();
	}

	@Override
	public String getUserName() {
		return bridge.userName();
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

	@JsonIgnore
	@Override // never return this id to the players
	public UUID getId() {
		return bridge.userId();
	}

	@Override
	public void onLeave() {
		this.endBalance = balance;
		LOGGER.fine("Player:" + getUserName() + " leaves table:" + table + " with money:" + endBalance + " started:" + initialBalance);
	}

	protected ReentrantLock getPlayerLock() {
		return playerLock;
	}

	@Override
	public int hashCode() {
		return Objects.hash(bridge.userId());
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
		return Objects.equals(bridge.userId(), other.bridge.userId());
	}

	@Override
	public PlayerStatus getStatus() {
		return status;
	}

	@Override
	public void setStatus(PlayerStatus status) {
		this.status = status;

	}

	@Override
	public BigDecimal getTotalBet() {
		return this.totalBet != null ? this.totalBet.setScale(2, RoundingMode.DOWN) : null;
	}

	@Override
	public <T> void sendMessage(T message) {
		if (!canSendMessage(message)) {
			LOGGER.log(Level.FINE, "Message cannot be delivered:" + message);
			return;
		}
		try {
			bridge.session().getBasicRemote().sendText(message.toString());
		} catch (IOException e) {
			UUID logIdentifier = UUID.randomUUID();
			LOGGER.log(Level.SEVERE, "Could not reach player: logIdentifier: " + logIdentifier + " name;" + getUserName() + " id:" + getId(), e);
			throw new RuntimeException("cannot be reached ->  LogId:" + logIdentifier);
		}
	}

	private <T> boolean canSendMessage(T message) {
		return bridge.isConnected() && message != null;
	}

	@Override
	public void updateStartingBet(BigDecimal bet, ICasinoTable table) {
		verifyCallersLock();
		// verifyStartingBet here is the last line of defence just before updating.
		BetVerifier.verifyStartingBet(table, this, bet);
		this.totalBet = bet;
	}

	protected void updateBalanceAndTotalBet(BigDecimal additionalBet) {
		verifyCallersLock();
		// additionalBet verification here is the last line of defence
		BetVerifier.verifySufficentBalance(additionalBet, this);
		if (this.totalBet == null)
			throw new IllegalBetException("updateBalanceAndTotalBet called but no initial bet was found", 6);
		this.balance = this.getBalance().subtract(additionalBet);
		this.totalBet = getTotalBet().add(additionalBet);
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

	public void increaseBalanceAndPayout(BigDecimal amount) {
		try {
			tryTakingPlayerLock();
			increaseBalance(amount);
			this.payout = payout.add(amount);
		} finally {
			releasePlayerLock();
		}
	}

	@Override
	public String toString() {
		return "CasinoPlayer [bridge=" + bridge + ", initialBalance=" + initialBalance + ", balance=" + balance + ", totalBet=" + totalBet + ", payout=" + payout + ", status=" + status + ", sitOutRounds=" + skippedBetRounds + "]";
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

	@JsonIgnore
	public Bridge getBridge() {
		return bridge;
	}

	@Override
	public void reset() {
		verifyCallersLock();
		this.totalBet = null;
	}

	@Override
	public void clearBetRoundSkips() {
		this.skippedBetRounds = 0;
	}

	@Override
	public void increaseBetRoundSkips() {
		this.skippedBetRounds++;
	}

	@Override
	public Integer getSitOutRounds() {
		return skippedBetRounds;
	}
	
	@Override
	public boolean isConnected() {
		return bridge.isConnected();
	}

	@Override
	public boolean shouldStandUp() {
		return this.status != PlayerStatus.ACTIVE && this.skippedBetRounds > getAllowedBetRoundSkips();
	}

	private Integer getAllowedBetRoundSkips() {
		return getTable().getThresholds().allowedBetRoundSkips();
	}
}
