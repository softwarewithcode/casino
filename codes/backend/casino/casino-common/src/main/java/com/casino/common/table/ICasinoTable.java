package com.casino.common.table;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.casino.common.bet.BetThresholds;
import com.casino.common.language.Language;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.table.phase.GamePhase;

public interface ICasinoTable {
	public boolean isClosed();

	public boolean isOpen();

	public boolean isMultiplayer();

	public boolean isPublic();

	public boolean isReserved();

	public boolean isPrivate();

	public void onClose();

	public void onPlayerLeave(ICasinoPlayer player);

	public void onPlayerTimeout(ICasinoPlayer player);

	public int getPlayerTurnTime();

	public void onTableClose();

	public boolean watch(ICasinoPlayer player);

	public PlayerRange getPlayerLimit();

	public Language getLanguage();

	public ICasinoPlayer getPlayerInTurn();

	public boolean addWatcher(ICasinoPlayer player);

	public boolean removePlayer(ICasinoPlayer p);

	public boolean removeWatcher(ICasinoPlayer p);

	public Set<ICasinoPlayer> getPlayers();

	public Set<ICasinoPlayer> getWatchers();

	public Set<ICasinoPlayer> getPlayersAndWatchers();

	public Status getStatus();

	public void setStatus(Status status);

	public Instant getCreated();

	public void onBetPhaseEnd();

	public BetThresholds getBetValues();

	public GamePhase getGamePhase();

	public UUID getId();

	public Clock getClock();

	public GamePhase updateGamePhase(GamePhase phase);

	boolean isGamePhase(GamePhase phase);

}
