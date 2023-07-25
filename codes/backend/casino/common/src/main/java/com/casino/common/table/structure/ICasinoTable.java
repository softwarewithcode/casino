package com.casino.common.table.structure;

import com.casino.common.dealer.BaseDealer;
import com.casino.common.game.phase.GamePhase;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.player.PlayerStatus;
import com.casino.common.table.TableStatus;
import com.casino.common.table.TableThresholds;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

public interface ICasinoTable {

    boolean isDealerTurn();

    void onClose();

    ICasinoPlayer getActivePlayer();

    boolean addWatcher(ICasinoPlayer player);

    Optional<ICasinoPlayer> removeWatcher(UUID id);

    List<? extends ICasinoPlayer> getPlayers();

    ConcurrentMap<UUID, ICasinoPlayer> getWatchers();

    TableStatus getStatus();

    void setStatus(TableStatus status);

    Instant getCreated();

    GamePhase getGamePhase();

    UUID getId();

    void startTiming(TimerTask task, long initialDelay);

    void stopClock();

    boolean isClockTicking();

    GamePhase updateGamePhase(GamePhase phase);

    boolean isGamePhase(GamePhase phase);

    BaseDealer getDealer();

    void updateCounterTime(int currentTime);

    int getCounterTime();

    void onActivePlayerChange(ICasinoPlayer player);

    TableThresholds getThresholds();

    default Integer getLeftPlayerCount() {
        return (int) getPlayers().stream().filter(player -> player.getStatus() == PlayerStatus.LEFT).count();
    }

    default Integer getSitOutPlayerCount() {
        return (int) getPlayers().stream().filter(player -> player.getStatus().isSitoutStatus()).count();
    }
}
