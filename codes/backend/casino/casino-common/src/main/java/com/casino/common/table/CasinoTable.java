package com.casino.common.table;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.UUID;

import com.casino.common.language.Language;
import com.casino.common.player.ICasinoPlayer;

/**
 * Base class for all casino tables. Gather here common data and operations what
 * could be related to all tables.
 * 
 */
public abstract class CasinoTable implements ICasinoTable {

	private Set<ICasinoPlayer> players;
	private Set<ICasinoPlayer> watchers;
	private ICasinoPlayer playerInTurn;
	private Status status;
	private int minPlayers;
	private int maxPlayers;
	private Type type;
	private BigDecimal minBet;
	private BigDecimal maxBet;
	private Language language;
	private Timer timer;
	private UUID id;

	protected CasinoTable(Status initialStatus, BigDecimal minBet, BigDecimal maxBet, int minPlayers, int maxPlayers, Type type, UUID id) {
		this.players = Collections.synchronizedSet(new HashSet<ICasinoPlayer>());
		this.watchers = Collections.synchronizedSet(new HashSet<ICasinoPlayer>());
		this.status = initialStatus;
		this.minBet = minBet;
		this.maxBet = maxBet;
		this.maxPlayers = maxPlayers;
		this.minPlayers = minPlayers;
		this.type = type;
		this.id = id;
	}

	@Override
	public abstract int getTurnTime();

	@Override
	public abstract int getComputerTurnTime();

	@Override
	public abstract void onPlayerLeave(ICasinoPlayer player);

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
		return maxPlayers > 1;
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
	public void onOpen() {
		// TODO Auto-generated method stub

	}

	@Override
	public int getMinPlayers() {
		return minPlayers;
	}

	@Override
	public int getMaxPlayers() {
		return maxPlayers;
	}

	@Override
	public BigDecimal getMinBet() {
		return minBet;
	}

	@Override
	public BigDecimal getMaxBet() {
		return maxBet;
	}

	@Override
	public Language getLanguage() {
		return language;
	}

	@Override
	public ICasinoPlayer getPlayerInTurn() {
		return playerInTurn;
	}

	@Override
	public boolean addPlayer(ICasinoPlayer player) {
		if (player == null) {
			return false;
		}
		if (players.size() >= maxPlayers) {
			return false;
		}
		return players.add(player);
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
		return player.getInitialBalance().compareTo(this.getMinBet()) >= 0;
	}

	@Override
	public void startTimer(int initialDelay) {
		// TODO Auto-generated method stub

	}

}
