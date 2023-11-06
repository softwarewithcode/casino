package com.casino.common.player;

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
import com.casino.common.functions.Functions;
import com.casino.common.table.structure.CasinoTable;
import com.casino.common.table.timing.PlayerTimeControl;
import com.casino.common.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.websocket.Session;

@JsonIgnoreProperties(value = { "id", "bridge" })
public abstract class Player implements CasinoPlayer {
	private static final Logger LOGGER = Logger.getLogger(Player.class.getName());
	protected final CasinoTable table;
	private final ReentrantLock playerLock;
	private final ReentrantLock messageLock;
	private final User user;
	private final PlayerTimeControl timeControl;
	private final Balance balance;
	private volatile BigDecimal totalBet;
	private volatile BigDecimal payout;

	private volatile PlayerStatus status;
	private volatile Integer skips = 0;

	public Player(User user, CasinoTable table) {
		super();
		this.user = user;
		this.balance = new Balance(user.initialBalance());
		this.status = null;
		this.payout = BigDecimal.ZERO;
		this.table = table;
		this.playerLock = new ReentrantLock();
		Integer timeBankAmount = table.getDealer().getGameData().getExtraTime();
		Integer playerTime = table.getDealer().getGameData().getPlayerTime();
		this.timeControl = new PlayerTimeControl(playerTime, timeBankAmount);
		this.messageLock = new ReentrantLock();
	}

	@Override
	public String getUserName() {
		return user.userName();
	}

	public CasinoTable getTable() {
		return table;
	}

	@JsonIgnore
	@Override // never return this id to the players
	public UUID getId() {
		return user.userId();
	}

	protected ReentrantLock getPlayerLock() {
		return playerLock;
	}

	@Override
	public int hashCode() {
		return Objects.hash(user.userId());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Player other = (Player) obj;
		return Objects.equals(user.userId(), other.user.userId());
	}

	@Override
	public PlayerStatus getStatus() {
		return status;
	}

	@Override
	public void setStatus(PlayerStatus status) {
		try {
			getPlayerLock().lock();
			this.status = status;
		} finally {
			getPlayerLock().unlock();
		}
	}

	@Override
	public BigDecimal getTotalBet() {
		return this.totalBet != null ? this.totalBet.setScale(2, RoundingMode.DOWN) : new BigDecimal("0").setScale(2, RoundingMode.DOWN);
	}

	@Override
	public BigDecimal getCurrentBalance() {
		return balance.getCurrent().setScale(2, RoundingMode.DOWN);
	}

	private void verifyCallersLock() {
		if (!playerLock.isHeldByCurrentThread())
			throw new IllegalBetException("lock is missing", 8);
	}

	protected void updateBalanceAndTotalBet(BigDecimal additionalBet) {
		verifyCallersLock();
		BetVerifier.verifySufficientBalance(additionalBet, this);
		balance.updateBalance(balance.getCurrent().subtract(additionalBet));
		this.totalBet = getTotalBet().add(additionalBet);
	}

	protected void updateTotalBet(BigDecimal bet) {
		verifyCallersLock();
		this.totalBet = bet;
	}

	@Override
	public void subtractTotalBetFromBalance() {
		try {
			tryLockOrThrow();
			if (totalBet == null || totalBet.compareTo(BigDecimal.ZERO) < 0)
				throw new IllegalBetException("Bet missing or negative:" + totalBet, 9);
			balance.subtract(totalBet);
		} finally {
			releaseLock();
		}
	}

	public void subtractFromBalance(BigDecimal amount) {
		try {
			tryLockOrThrow();
			BetVerifier.verifySufficientBalance(amount, this);
			balance.subtract(amount);
		} finally {
			releaseLock();
		}
	}

	protected boolean hasBalance() {
		return Functions.isFirstMoreThanSecond.apply(getCurrentBalance(), BigDecimal.ZERO);
	}

	@Override
	public void increaseBalance(BigDecimal amount) {
		try {
			tryLockOrThrow();
			if (Functions.isFirstMoreThanSecond_(BigDecimal.ZERO, amount))
				throw new IllegalArgumentException("wrong increase amount " + amount);
			balance.add(amount);
		} finally {
			releaseLock();
		}
	}

	@Override
	public void increaseBalanceAndPayout(BigDecimal amount) {
		try {
			tryLockOrThrow();
			increaseBalance(amount);
			this.payout = payout.add(amount);
		} finally {
			releaseLock();
		}
	}

	@Override
	public String toString() {
		return "CasinoPlayer [bridge=" + user + ", initialBalance=" + balance.getInitialBalance() + ", balance=" + balance + ", totalBet=" + totalBet + ", payout=" + payout + ", status=" + status + ", sitOutRounds=" + skips + "]";
	}

	@Override
	public void removeTotalBet() {
		try {
			tryLockOrThrow();
			this.totalBet = null;
		} finally {
			if (playerLock.isHeldByCurrentThread())
				playerLock.unlock();
		}
	}

	@Override
	public boolean hasBet() {
		return this.totalBet != null && totalBet.compareTo(BigDecimal.ZERO) > 0;
	}

	@Override
	public void reset() {
		verifyCallersLock();
		this.totalBet = BigDecimal.ZERO;
		payout = BigDecimal.ZERO;
	}

	@Override
	public void clearSkips() {
		try {
			tryLockOrThrow();
			this.skips = 0;
		} finally {
			releaseLock();
		}

	}

	@Override
	public void increaseSkips() {
		try {
			tryLockOrThrow();
			this.skips++;
		} finally {
			releaseLock();
		}

	}

	@Override
	public boolean isConnected() {
		return user.isConnected();
	}

	@Override
	public Session getSession() {
		return user.getSession();
	}

	public boolean isActive() {
		return this.status == PlayerStatus.ACTIVE;
	}

	public boolean isNew() {
		return getStatus() == PlayerStatus.NEW;
	}

	public boolean isSitOut() {
		return getStatus() == PlayerStatus.SIT_OUT;
	}

	@Override
	public boolean shouldStandUp() {
		return this.status != PlayerStatus.ACTIVE && this.skips > getAllowedSkipCount();
	}

	@Override
	public boolean hasTooManySkips() {
		return this.skips > getAllowedSkipCount();
	}

	private Integer getAllowedSkipCount() {
		return table.getDealer().getGameData().getMaxSkips();
	}

	public PlayerTimeControl getTimeControl() {
		return timeControl;
	}

	protected void releaseLock() {
		if (getPlayerLock().isHeldByCurrentThread())
			getPlayerLock().unlock();
	}

	protected void tryLockOrThrow() {
		if (!getPlayerLock().tryLock())
			throw new ConcurrentModificationException("playerLock was not obtained. Timing error" + playerLock + " t:" + Thread.currentThread());
	}

	protected void takeLock() {
		getPlayerLock().lock();
	}

	@Override
	public <T> void sendMessage(T message) {
		if (!isReachable(message)) 
			return;
		try {
			messageLock.lock();
			getSession().getBasicRemote().sendText(message.toString());
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while sending message ", e);
		} finally {
			messageLock.unlock();
		}
	}

	private <T> boolean isReachable(T message) {
		return isConnected() && message != null;
	}
}
