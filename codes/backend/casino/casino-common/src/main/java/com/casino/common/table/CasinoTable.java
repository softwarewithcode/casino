package com.casino.common.table;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.UUID;

import com.casino.common.language.Language;
import com.casino.common.player.BetLimit;
import com.casino.common.player.ICasinoPlayer;

/**
 * Base class for all casino tables. Gather here common data and operations what
 * could be related to all tables.
 * 
 */
public abstract class CasinoTable implements ICasinoTable {

	private Set<ICasinoPlayer> players;
	private Set<ICasinoPlayer> watchers;
	private Status status;
	private PlayerRange playerLimit;
	private BetLimit betLimit;
	private Type type;
	private Language language;
	private Timer timer;
	private UUID id;
	private Instant created;

	protected CasinoTable(Status initialStatus, BetLimit betLimit, PlayerRange playerLimit, Type type, UUID id) {
		this.players = Collections.synchronizedSet(new HashSet<ICasinoPlayer>());
		this.watchers = Collections.synchronizedSet(new HashSet<ICasinoPlayer>());
		this.status = initialStatus;
		this.type = type;
		this.id = id;
		this.created = Instant.now();
		this.betLimit = betLimit;
		this.playerLimit = playerLimit;
	}

	@Override
	public abstract int getTurnTime();

	@Override
	public abstract int getComputerTurnTime();

	@Override
	public abstract void onPlayerLeave(ICasinoPlayer player);

	protected boolean joinAsWatcher(ICasinoPlayer player) {
		if (player == null)
			return false;
		watchers.add(player);
		return true;
	}

	protected boolean joinAsPlayer(ICasinoPlayer player) {
		if (player == null)
			return false;
		players.add(player);
		return true;
	}

	@Override
	public boolean isClosed() {
		return status == Status.CLOSED;
	}

	@Override
	public boolean isGathering() {
		return status == Status.GATHERING;
	}

	@Override
	public boolean isMultiplayer() {
		return playerLimit.maximumPlayers() > 1;
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
	public void close() {
		status = Status.CLOSED;
	}

	@Override
	public void open() {
		status = Status.OPEN;
	}

	@Override
	public boolean isOpen() {
		return status == Status.OPEN;
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
	public PlayerRange getPlayerLimit() {
		return playerLimit;
	}

	@Override
	public BetLimit getBetLimit() {
		return betLimit;
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

	@Override
	public Set<ICasinoPlayer> getPlayers() {
		return players;
	}

	@Override
	public Set<ICasinoPlayer> getWatchers() {
		return watchers;
	}

	@Override
	public Set<ICasinoPlayer> getPlayersAndWatchers() {
		HashSet<ICasinoPlayer> set = new HashSet<ICasinoPlayer>();
		set.addAll(players);
		set.addAll(watchers);
		return set;
	}

	protected boolean coversMinimumBet(ICasinoPlayer player) {
		return player.getInitialBalance().compareTo(this.getBetLimits().minimumBet()) >= 0;
	}

	@Override
	public void startTimer(int initialDelay) {
		// TODO Auto-generated method stub

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

	public BetLimit getBetLimits() {
		return betLimit;
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
		CasinoTable other = (CasinoTable) obj;
		return Objects.equals(id, other.id);
	}

}
