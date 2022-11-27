package com.casino.common.table;

import java.math.BigDecimal;
import java.util.Set;

import com.casino.common.language.Language;
import com.casino.common.player.ICasinoPlayer;

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

	public void onPlayerLeave(ICasinoPlayer player);

	public int getMinPlayers();

	public int getMaxPlayers();

	public BigDecimal getMinBet();

	public BigDecimal getMaxBet();

	public Language getLanguage();

	public ICasinoPlayer getPlayerInTurn();

	public boolean addPlayer(ICasinoPlayer player);

	public boolean addWatcher(ICasinoPlayer player);

	public boolean removePlayer(ICasinoPlayer p);

	public boolean removeWatcher(ICasinoPlayer p);

	public Set<ICasinoPlayer> getPlayers();

	public Set<ICasinoPlayer> getWatchers();

	public Set<ICasinoPlayer> getPlayersAndWatchers();

	public int getComputerTurnTime();

	public int getTurnTime();

	public void startTimer(int initialDelay);

	public void onTimeout(ICasinoPlayer player);
}
