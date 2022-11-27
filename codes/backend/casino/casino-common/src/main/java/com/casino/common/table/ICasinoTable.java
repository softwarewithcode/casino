package com.casino.common.table;

import java.math.BigDecimal;
import java.util.Set;

import com.casino.common.language.Language;
import com.casino.common.player.IPlayer;

public interface ICasinoTable {
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

	public void onPlayerLeave(IPlayer player);

	public int getMinPlayers();

	public int getMaxPlayers();

	public BigDecimal getMinBet();

	public BigDecimal getMaxBet();

	public Language getLanguage();

	public IPlayer getPlayerInTurn();

	public boolean addPlayer(IPlayer player);

	public boolean addWatcher(IPlayer player);

	public boolean removePlayer(IPlayer p);

	public boolean removeWatcher(IPlayer p);

	public Set<IPlayer> getPlayers();

	public Set<IPlayer> getWatchers();

	public Set<IPlayer> getPlayersAndWatchers();

	public int getComputerTurnTime();

	public int getTurnTime();

	public void startTimer(int initialDelay);

	public void onTimeout(IPlayer player);
}
