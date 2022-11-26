package com.casino.common.table;

import java.math.BigDecimal;
import java.util.Set;

import com.casino.common.language.Language;

public interface ITable {
	public boolean isClosed();

	public void close();

	public void open();

	public boolean isOpen();

	public boolean isMultiplayer();

	public boolean isGathering();

	public boolean isPublic();

	public boolean isReserved();

	public boolean isPrivate();

	public void onClose();

	public void onOpen();

	public void onPlayerLeave(Player player);

	public int getMinPlayers();

	public int getMaxPlayers();

	public BigDecimal getMinBet();

	public BigDecimal getMaxBet();

	public int getAvailableSeats();

	public Language getLanguage();

	public Player getPlayerInTurn();

	public boolean addPlayer(Player player);

	public boolean addWatcher(Player player);

	public boolean removePlayer(Player p);

	public boolean removeWatcher(Player p);

	public Set<Player> getPlayers();

	public Set<Player> getWatchers();

	public Set<Player> getPlayersAndWatchers();

	public int getComputerTurnTime();

	public int getTurnTime();

	public void startTimer(int initialDelay);

	public void onTimeout(Player player);
}
