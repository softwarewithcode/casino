package com.casino.common.table;

import java.time.Instant;
import java.util.ConcurrentModificationException;
import java.util.Objects;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import com.casino.common.language.Language;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.table.phase.GamePhase;
import com.casino.common.table.phase.PhasePath;
import com.casino.common.table.timing.Clock;
import com.casino.common.table.timing.PlayerClockTask;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * @author softwarewithcode from GitHub
 *
 */

public abstract class CasinoTable implements ICasinoTable {

	// private static final Logger LOGGER =
	// Logger.getLogger(CasinoTable.class.getName());
	@JsonIgnore
	private final PhasePath phasePath;
	@JsonProperty
	private final Type type;
	@JsonIgnore
	private final ConcurrentHashMap<UUID, ICasinoPlayer> watchers;
	@JsonIgnore
	private final ReentrantLock playerInTurnLock;
	@JsonProperty
	private final UUID id;
	@JsonIgnore
	private final Instant created;
	@JsonIgnore
	private final Clock clock;
	@JsonProperty
	private Language language;
	@JsonProperty
	private ICasinoPlayer playerInTurn;
	@JsonProperty
	private boolean dealerTurn;
	@JsonIgnore
	private volatile Status status;
	@JsonIgnore
	private TableInitData tableInitData;
	@JsonProperty
	private final TableCard tableCard;

	protected CasinoTable(Status initialStatus, TableInitData initData, PhasePath phases) {
		this.watchers = new ConcurrentHashMap<>();
		this.status = initialStatus;
		this.type = initData.tableType();
		this.id = initData.id();
		this.created = Instant.now();
		this.clock = new Clock();
		this.phasePath = phases;
		this.playerInTurnLock = new ReentrantLock(true);
		this.tableInitData = initData;
		tableCard = new TableCard(initData);
	}

	protected <T extends ICasinoPlayer> boolean joinAsWatcher(T watcher) {
		if (watcher == null)
			return false;
		return watchers.putIfAbsent(watcher.getId(), watcher) == null;
	}

	public synchronized void startClock(TimerTask task, long initialDelay) {
		if (this.clock.isTicking())
			throw new IllegalArgumentException("Table clock already running, timing error");
		this.clock.startClock(task, initialDelay, 1000);
	}

	@Override
	public synchronized void stopClock() {
		this.clock.stopClock();
	}

	@JsonIgnore
	@Override
	public boolean isClockTicking() {
		return this.clock.isTicking();
	}

	@JsonIgnore
	@Override
	public boolean isClosed() {
		return status == Status.CLOSED;
	}

	@Override
	public boolean isMultiplayer() {
		return tableCard.getThresholds().maxPlayers() > 1;
	}

	@JsonIgnore
	@Override
	public boolean isPublic() {
		return type == Type.PUBLIC;
	}

	@JsonIgnore
	@Override
	public boolean isPrivate() {
		return type == Type.PRIVATE;
	}

	@JsonIgnore
	@Override
	public boolean isReserved() {
		return type == Type.RESERVED;
	}

	@JsonIgnore
	public Thresholds getThresholds() {
		return getTableCard().getThresholds();
	}

	@Override
	public boolean isOpen() {
		return status == Status.WAITING_PLAYERS || status == Status.RUNNING;
	}

	@Override
	public void onClose() {
		// TODO Auto-generated method stub

	}

	@JsonProperty
	public int getWatcherCount() {
		return watchers.size();
	}

	@Override
	public Language getLanguage() {
		return language;
	}

	@Override
	public boolean addWatcher(ICasinoPlayer watcher) {
		if (watcher == null)
			return false;
		return watchers.putIfAbsent(watcher.getId(), watcher) != null;
	}

	public ReentrantLock getPlayerInTurnLock() {
		return playerInTurnLock;
	}

	@Override
	public boolean isDealerTurn() {
		return dealerTurn;
	}

	public void updateDealerTurn(boolean turn) {
		this.dealerTurn = turn;
	}

	@Override
	public void removeWatcher(UUID watcherId) {
		if (watcherId == null)
			return;
		watchers.remove(watcherId);
	}

	@Override
	public ConcurrentMap<UUID, ICasinoPlayer> getWatchers() {
		return watchers;
	}

	protected boolean isAcceptingPlayers() {
		return getStatus() == Status.WAITING_PLAYERS || getStatus() == Status.RUNNING;
	}

	@Override
	public Status getStatus() {
		return status;
	}

	@Override
	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public Instant getCreated() {
		return created;
	}

	public Type getType() {
		return type;
	}

	@Override
	public UUID getId() {
		return tableInitData.id();
	}

	public GamePhase updateGamePhase(GamePhase phase) {
		phasePath.setCurrentPhase(phase);
		return phasePath.getPhase();
	}

	@Override
	public ICasinoPlayer getPlayerInTurn() {
		return playerInTurn;
	}

	public void updateCounterTime(int counter) {
		this.clock.updateTime(counter);
	}

	public int getCounterTime() {
		return this.clock.getTime();
	}

	@JsonProperty
	public GamePhase getPhase() {
		return phasePath.getPhase();
	}

	protected boolean isPlayerInTurn(ICasinoPlayer player) {
		if (player == null)
			return false;
		return getPlayerInTurn() != null && getPlayerInTurn().equals(player);
	}

	public void changePlayer(ICasinoPlayer player) {
		System.out.println("ChangePlayer to:" + player.getName() + " stat " + player.getStatus());
		if (playerInTurnLock.isHeldByCurrentThread()) {
			playerInTurn = player;
			if (playerInTurn != null) {
				stopClock();
				PlayerClockTask playerTimer = new PlayerClockTask(this, player);
				startClock(playerTimer, 0);
			}
		} else
			throw new ConcurrentModificationException("Cannot update playerInTurn, lock is missing");
	}

	public void clearPlayerInTurn() {
		this.playerInTurn = null;
	}

	@Override
	public boolean isGamePhase(GamePhase phase) {
		return getGamePhase() == phase;
	}

	@Override
	public GamePhase getGamePhase() {
		return phasePath.getPhase();
	}

	public boolean isRoundRunning() {
		return phasePath.getPhase().isOnGoingRound();
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	public TableCard getTableCard() {
		return tableCard;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CasinoTable other = (CasinoTable) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return "CasinoTable [type=" + type + ", id=" + id + ", created=" + created + ", playerInTurn=" + playerInTurn + ", tableInitData=" + tableInitData + "]";
	}

}