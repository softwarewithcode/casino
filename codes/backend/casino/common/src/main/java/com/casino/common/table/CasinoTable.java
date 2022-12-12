package com.casino.common.table;

import java.time.Instant;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.TimerTask;
import java.util.UUID;
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
	private Set<ICasinoPlayer> players;
	private Set<ICasinoPlayer> watchers;
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
		this.players = Collections.synchronizedSet(new HashSet<ICasinoPlayer>());
		this.watchers = Collections.synchronizedSet(new HashSet<ICasinoPlayer>());
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

	protected boolean joinAsWatcher(ICasinoPlayer player) {
		if (player == null)
			return false;
		if (players.contains(player)) {
			LOGGER.log(Level.INFO, "Player is playing, tries to join as a watcher " + player.getName());
			return false;
		}
		watchers.add(player);
		return true;
	}

	protected boolean joinAsPlayer(ICasinoPlayer player) {
		if (player == null)
			return false;
		players.add(player);
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
		return status == Status.WAITING_PLAYERS;
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
	public boolean addWatcher(ICasinoPlayer player) {
		if (player == null)
			return false;
		return watchers.add(player);
	}

	@Override
	public boolean removePlayer(ICasinoPlayer p) {
		if (p == null)
			return false;
		return players.remove(p);
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
		return watchers.remove(p);
	}

	protected void changeFromWatcherToPlayer(ICasinoPlayer player) {
		watchers.remove(player);
		players.add(player);
	}

	public void changeFromPlayerToWatcher(ICasinoPlayer player) {
		players.remove(player);
		watchers.add(player);
	}

	@Override
	public Set<ICasinoPlayer> getPlayers() {
		return players;
	}

	@Override
	public Set<ICasinoPlayer> getWatchers() {
		return watchers;
	}

	protected boolean isAcceptingPlayers() {
		return getStatus() == Status.WAITING_PLAYERS || getStatus() == Status.RUNNING;
	}

	@Override
	public Set<ICasinoPlayer> getPlayersAndWatchers() {
		HashSet<ICasinoPlayer> set = new HashSet<ICasinoPlayer>();
		set.addAll(players);
		set.addAll(watchers);
		return set;
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

}
