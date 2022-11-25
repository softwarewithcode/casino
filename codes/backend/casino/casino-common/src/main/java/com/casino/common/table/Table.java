package com.casino.common.table;

import java.math.BigDecimal;
import java.util.List;

import com.casino.common.language.Language;

public interface Table {
	public boolean isClosed();

	public void close();

	public void open();

	public boolean isOpen();

	public void onClose();

	public void onOpen();

	public int getMinPlayers();

	public int getMaxPlayers();

	public BigDecimal getMinBet();

	public BigDecimal getMaxBet();

	public int getAvailableSeats();

	public Language getLanguage();

	public Player getPlayerInTurn();

	public void addPlayer(Player player);

	public void addWatcher(Player player);

	public void removePlayer(Player p);

	public void removeWatcher(Player p);

	public List<Player> getPlayers();

	public List<Player> getWatchers();

	public List<Player> getPlayersAndWatchers();

	public int getAITurnTimeInMillis();

	public int getPlayerTurnTimeInMillis();

	public void startTime(int initialDelay);
}
