package com.casino.common.table;

import java.time.Instant;
import java.util.Set;

import com.casino.common.bet.BetInfo;
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

	public void onPlayerLeave(ICasinoPlayer player);

	public boolean join(ICasinoPlayer player);

	public PlayerRange getPlayerLimit();

	public BetInfo getBetInfo();

	public Language getLanguage();

	public ICasinoPlayer getPlayerInTurn();

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

	public Status getStatus();

	public void setStatus(Status status);

	public Instant getCreated();

	public void onBetRoundEnd();
}
