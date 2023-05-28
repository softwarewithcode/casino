package com.casino.common.player;

import com.casino.common.bet.BetVerifier;
import com.casino.common.exception.IllegalBetException;
import com.casino.common.functions.Functions;
import com.casino.common.table.structure.ICasinoTable;
import com.casino.common.table.timing.Time;
import com.casino.common.user.Bridge;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ConcurrentModificationException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

@JsonIgnoreProperties(value = {"id", "bridge"})
public abstract class CasinoPlayer implements ICasinoPlayer {
    private static final Logger LOGGER = Logger.getLogger(CasinoPlayer.class.getName());
    protected final ICasinoTable table;
    private final ReentrantLock playerLock;
    private final Bridge bridge;
    private final Time timeControl;
    private volatile Balance balance;
    protected volatile BigDecimal totalBet;
    private volatile BigDecimal payout;

    private volatile PlayerStatus status;
    private volatile Integer skips = 0;

    public CasinoPlayer(Bridge bridge, ICasinoTable table) {
        super();
        this.bridge = bridge;
        this.balance = new Balance(bridge.initialBalance());
        this.status = null;
        this.payout = BigDecimal.ZERO;
        this.table = table;
        this.playerLock = new ReentrantLock();
        Integer timeBankAmount = table.getDealer().getGameData().getExtraTime();
        Integer playerTime = table.getDealer().getGameData().getPlayerTime();
        this.timeControl = new Time(playerTime, timeBankAmount);
    }

    @Override
    public String getUserName() {
        return bridge.userName();
    }

    public ICasinoTable getTable() {
        return table;
    }

    @JsonIgnore
    @Override // never return this id to the players
    public UUID getId() {
        return bridge.userId();
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

    @Override
    public void subtractTotalBetFromBalance() {
        try {
            tryLock();
            if (totalBet == null || totalBet.compareTo(BigDecimal.ZERO) < 0)
                throw new IllegalBetException("Bet missing or negative:" + totalBet, 9);
            balance.subtract(totalBet);
        } finally {
            releaseLock();
        }
    }

    public void subtractFromBalance(BigDecimal amount) {
        try {
            tryLock();
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
            tryLock();
            balance.add(amount);
        } finally {
            releaseLock();
        }
    }

    @Override
    public void increaseBalanceAndPayout(BigDecimal amount) {
        try {
            tryLock();
            increaseBalance(amount);
            this.payout = payout.add(amount);
        } finally {
            releaseLock();
        }
    }

    @Override
    public String toString() {
        return "CasinoPlayer [bridge=" + bridge + ", initialBalance=" + balance.getInitialBalance() + ", balance=" + balance + ", totalBet=" + totalBet + ", payout=" + payout + ", status=" + status + ", sitOutRounds=" + skips + "]";
    }

    @Override
    public void removeTotalBet() {
        try {
            tryLock();
            this.totalBet = null;
        } finally {
            if (playerLock.isHeldByCurrentThread())
                playerLock.unlock();
        }
    }

    public boolean hasBet() {
        return this.totalBet != null && totalBet.compareTo(BigDecimal.ZERO) > 0;
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
    public void clearSkips() {
        this.skips = 0;
    }

    @Override
    public void increaseSkips() {
        this.skips++;
    }

    @Override
    public boolean isConnected() {
        return bridge.isConnected();
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

    private Integer getAllowedSkipCount() {
        return table.getDealer().getGameData().getMaxSkips();
    }

    public Time getTimeControl() {
        return timeControl;
    }

    protected void releaseLock() {
        if (getPlayerLock().isHeldByCurrentThread())
            getPlayerLock().unlock();
    }

    protected void tryLock() {
        if (!getPlayerLock().tryLock())
            throw new ConcurrentModificationException("playerLock was not obtained. Timing error" + playerLock + " t:" + Thread.currentThread());
    }

    protected void takeLock() {
        getPlayerLock().lock();
    }

    protected ReentrantLock getLock() {
        return getPlayerLock();
    }
}
