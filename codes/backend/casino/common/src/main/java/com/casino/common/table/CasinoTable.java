package com.casino.common.table;

import java.time.Instant;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.common.language.Language;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.table.phase.GamePhase;
import com.casino.common.table.phase.PhasePath;
import com.casino.common.table.timing.Clock;
import com.casino.common.table.timing.PlayerClockTask;

/**
 * Base class for all casino tables. Gather here common data and operations what
 * could be related to all tables.
 * 
 */
public abstract class CasinoTable implements ICasinoTable {

	private static final Logger LOGGER = Logger.getLogger(CasinoTable.class.getName());
	private final PhasePath phasePath;
	private ConcurrentHashMap<UUID, ICasinoPlayer> players;
	private ConcurrentHashMap<UUID, ICasinoPlayer> watchers;
	private volatile Status status;
	private Type type;
	private Language language;
	private final UUID id;
	private final Instant created;
	private final Clock clock;
	private ICasinoPlayer playerInTurn;
	private final ReentrantLock playerInTurnLock;
	private boolean dealerTurn;
	private final Thresholds constants;

	protected CasinoTable(Status initialStatus, Thresholds tableConstants, UUID id, PhasePath phases) {
		this.players = new ConcurrentHashMap<>();
		this.watchers = new ConcurrentHashMap<>();
		this.status = initialStatus;
		this.type = tableConstants.tableType();
		this.id = id;
		this.created = Instant.now();
		this.constants = tableConstants;
		this.clock = new Clock();
		this.phasePath = phases;
		this.playerInTurnLock = new ReentrantLock(true);
	}

	@Override
	public abstract void onPlayerLeave(ICasinoPlayer player);

	protected <T extends ICasinoPlayer> boolean joinAsWatcher(T watcher) {
		if (watcher == null)
			return false;
		if (players.contains(watcher)) {
			LOGGER.log(Level.INFO, "Player is playing, tries to join as a watcher " + watcher.getName());
			return false;
		}
		watchers.putIfAbsent(watcher.getId(), watcher);
		return true;
	}

	protected boolean joinAsPlayer(ICasinoPlayer player) {
		if (player == null)
			return false;
		players.putIfAbsent(player.getId(), player);
		return true;
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

	@Override
	public boolean isClockTicking() {
		return this.clock.isTicking();
	}

	@Override
	public boolean isClosed() {
		return status == Status.CLOSED;
	}

	@Override
	public boolean isMultiplayer() {
		return constants.maxPlayers() > 1;
	}

	@Override
	public boolean isPublic() {
		return type == Type.PUBLIC;
	}

	@Override
	public boolean isPrivate() {
		return type == Type.PRIVATE;
	}

	@Override
	public boolean isReserved() {
		return type == Type.RESERVED;
	}

	@Override
	public boolean isOpen() {
		return status == Status.WAITING_PLAYERS || status == Status.RUNNING;
	}

	@Override
	public void onClose() {
		// TODO Auto-generated method stub

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

	@Override
	public boolean removePlayer(ICasinoPlayer p) {
		if (p == null)
			return false;
		return players.remove(p.getId()) != null;
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
	public boolean removeWatcher(ICasinoPlayer p) {
		if (p == null)
			return false;
		return watchers.remove(p.getId()) != null;
	}

	protected void changeFromWatcherToPlayer(ICasinoPlayer player) {
		watchers.remove(player.getId());
		players.putIfAbsent(player.getId(), player);
	}

	public void changeFromPlayerToWatcher(ICasinoPlayer player) {
		players.remove(player.getId());
		watchers.putIfAbsent(player.getId(), player);
	}

	@Override
	public ConcurrentMap<UUID, ICasinoPlayer> getPlayers() {
		return players;
	}

	@Override
	public ConcurrentMap<UUID, ICasinoPlayer> getWatchers() {
		return watchers;
	}

	@Override
	public Set<ICasinoPlayer> tempConcurTest() {
		Set<ICasinoPlayer> m = new HashSet<>();
		m.addAll(players.values());
		m.addAll(watchers.values());
		return m;
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

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public UUID getId() {
		return id;
	}

	public GamePhase updateGamePhase(GamePhase phase) {
		phasePath.setCurrentPhase(phase);
		return phasePath.getPhase();
	}

	@Override
	public String toString() {
		return "CasinoTable [status=" + status + ", type=" + type + ", id=" + id + ", playerInTurn=" + playerInTurn + "]";
	}

	@Override
	public ICasinoPlayer getPlayerInTurn() {
		return playerInTurn;
	}

	protected boolean isPlayerInTurn(ICasinoPlayer player) {
		if (player == null)
			return false;
		return getPlayerInTurn() != null && getPlayerInTurn().equals(player);
	}

	public void changePlayer(ICasinoPlayer player) {
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

	@Override
	public boolean isGamePhase(GamePhase phase) {
		return getGamePhase() == phase;
	}

	@Override
	public GamePhase getGamePhase() {
		return phasePath.getPhase();
	}

	public Thresholds getThresholds() {
		return constants;
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

	protected <T> void notifyTable(T t) {
		getPlayers().entrySet().parallelStream().forEach(o -> o.getValue().sendMessage(t));
//		Thread.ofVirtual().start(() -> { commenting this part out as this is 'production code' and VirtualThreads are still a preview feature.
			getWatchers().entrySet().parallelStream().forEach(o -> o.getValue().sendMessage(t));
//		});
	}

	protected <T, P extends ICasinoPlayer> void notifyPlayer(T t, P p) {
		ICasinoPlayer player = getPlayers().get(p.getId());
		if (player != null)
			player.sendMessage(t);
	}
}