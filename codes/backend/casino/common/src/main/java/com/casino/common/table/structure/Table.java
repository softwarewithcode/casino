package com.casino.common.table.structure;

import java.time.Instant;
import java.util.ConcurrentModificationException;
import java.util.Objects;
import java.util.Optional;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import com.casino.common.exception.TableClockException;
import com.casino.common.game.phase.GamePhase;
import com.casino.common.game.phase.PhasePath;
import com.casino.common.player.CasinoPlayer;
import com.casino.common.player.PlayerTimingTask;
import com.casino.common.table.TableData;
import com.casino.common.table.TableStatus;
import com.casino.common.table.TableThresholds;
import com.casino.common.table.timing.Clock;
import com.casino.common.user.Connectable;
import com.casino.common.user.User;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;

/**
 * @author softwarewithcode from GitHub
 */
@JsonIncludeProperties(value = { /* implementing subclass defines exact fields */})
public abstract class Table implements CasinoTable {

    protected final PhasePath phasePath;
    private final TableType type;
    private final ConcurrentHashMap<UUID, Connectable> watchers;
    private final ReentrantLock lock;
    private final UUID id;
    private final Instant created;
    private final Clock clock;
    private final TableData tableData;
    private CasinoPlayer activePlayer;
    private boolean dealerTurn;
    private volatile TableStatus status;

    protected Table(TableData tableData) {
        this.watchers = new ConcurrentHashMap<>();
        this.status = tableData.initialStatus();
        this.type = tableData.tableType();
        this.id = tableData.id();
        this.created = Instant.now();
        this.clock = new Clock();
        this.phasePath = tableData.phases();
        this.lock = new ReentrantLock(true);
        this.tableData = tableData;
    }

    protected boolean joinAsWatcher(Connectable watcher) {
        if (watcher == null)
            return false;
        return watchers.putIfAbsent(watcher.getId(), watcher) == null;
    }

    public synchronized void startTiming(TimerTask task, long startDelay) {
        if (this.clock.isTicking())
            throw new TableClockException("Table clock already running, timing error");
        this.clock.startClock(task, startDelay, 1000);
    }

    @Override
    public synchronized void stopTiming() {
        this.clock.stopClock();
    }

    @Override
    public boolean isClockTicking() {
        return this.clock.isTicking();
    }

    private void startPlayerClock(CasinoPlayer player) {
        PlayerTimingTask playerTimer = new PlayerTimingTask(getDealer(), player);
        startTiming(playerTimer, 0);
    }

    @Override
    public void onClose() {
        stopTiming();
        getWatchers().clear();
        activePlayer = null;
        dealerTurn = false;
        setStatus(TableStatus.CLOSED);
    }

    @Override
    public boolean addWatcher(User watcher) {
        if (watcher == null || !watcher.isConnected())
            return false;
        return watchers.putIfAbsent(watcher.userId(), watcher) != null;
    }

    public ReentrantLock getLock() {
        return lock;
    }

    protected void verifyCallersLock() {
        if (!getLock().isHeldByCurrentThread())
            throw new IllegalStateException("lock is missing");
    }

    @Override
    public boolean isDealerTurn() {
        return dealerTurn;
    }
    @Override
    public boolean isMultiplayer() {
        return getType()==TableType.MULTIPLAYER;
    }
    public void updateDealerTurn(boolean turn) {
        this.dealerTurn = turn;
    }

    @Override
    public void removeWatcher(UUID watcherId) {
        watchers.remove(watcherId);
    }

    @Override
    public ConcurrentMap<UUID, Connectable> getWatchers() {
        return watchers;
    }
    protected Optional<Connectable> getWatcher(UUID id) {
    	return Optional.ofNullable(watchers.get(id));
    }

    @Override
    public TableStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(TableStatus status) {
        this.status = status;
    }

    @Override
    public Instant getCreated() {
        return created;
    }

    public TableType getType() {
        return type;
    }

    @Override
    public UUID getId() {
        return tableData.id();
    }

    @Override
    public GamePhase updateGamePhase(GamePhase phase) {
        phasePath.setCurrentPhase(phase);
        return phasePath.getPhase();
    }

    @Override
    public CasinoPlayer getActivePlayer() {
        return activePlayer;
    }

    public void updateCounterTime(int counterTime) {
        this.clock.updateTime(counterTime);
    }

    public Integer getCounterTime() {
        return this.clock.getTime();
    }

    public boolean isActivePlayer(CasinoPlayer player) {
        if (player == null)
            return false;
        return getActivePlayer() != null && getActivePlayer().equals(player);
    }

    @Override
    public void onActivePlayerChange(CasinoPlayer player) {
        stopTiming();
        if (player == null)
            throw new IllegalArgumentException("no player ");
        if (!lock.isHeldByCurrentThread())
            throw new ConcurrentModificationException("playerInTurnLock is missing " + player);
        if (activePlayer != null)
            activePlayer.clearAvailableActions();
        activePlayer = player;
        player.updateAvailableActions();
        activePlayer.getTimeControl().initPlayerTime(getDealer().getGameData().getPlayerTime());
        startPlayerClock(player);
    }

    public void clearActivePlayer() {
        this.activePlayer = null;
    }

    @Override
    public boolean isGamePhase(GamePhase phase) {
        return getGamePhase() == phase;
    }

    public boolean isRoundRunning() {
        return phasePath.getPhase().isGameRunning();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public TableThresholds getThresholds() {
        return tableData.thresholds();
    }

    public PhasePath getPhasePath() {
        return phasePath;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Table other = (Table) obj;
        return Objects.equals(id, other.id);
    }


    @Override
    public String toString() {
        return "CasinoTable [type=" + type + ", id=" + id + ", created=" + created + ", playerInTurn=" + activePlayer + ", tableData=" + tableData + "]";
    }

}