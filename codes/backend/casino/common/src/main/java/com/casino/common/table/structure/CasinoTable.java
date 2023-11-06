package com.casino.common.table.structure;

import java.time.Instant;
import java.util.List;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import com.casino.common.dealer.Croupier;
import com.casino.common.game.phase.GamePhase;
import com.casino.common.player.CasinoPlayer;
import com.casino.common.player.PlayerStatus;
import com.casino.common.table.TableStatus;
import com.casino.common.table.TableThresholds;
import com.casino.common.user.Connectable;
import com.casino.common.user.User;

public interface CasinoTable {

    boolean isDealerTurn();

    void onClose();

    CasinoPlayer getActivePlayer();

    boolean addWatcher(User user);

    void removeWatcher(UUID id);

    List<? extends CasinoPlayer> getPlayers();

    ConcurrentMap<UUID, Connectable> getWatchers();

    TableStatus getStatus();

    void setStatus(TableStatus status);

    Instant getCreated();

    GamePhase getGamePhase();

    UUID getId();

    void startTiming(TimerTask task, long initialDelay);

    void stopTiming();

    boolean isClockTicking();

    GamePhase updateGamePhase(GamePhase phase);

    boolean isGamePhase(GamePhase phase);

    <V extends Croupier> V getDealer();

    void updateCounterTime(int timeSeconds);

    Integer getCounterTime();

    void onActivePlayerChange(CasinoPlayer player);

    TableThresholds getThresholds();

    default Integer getSitOutPlayerCount() {
        return (int) getPlayers().stream().filter(player -> player.getStatus().isSitoutStatus()).count();
    }

    default Integer getPlayersCountWithStatus(PlayerStatus status) {
        return (int) getPlayers().stream().filter(player -> player.getStatus() == status).count();
    }
    

    boolean isMultiplayer();
}
