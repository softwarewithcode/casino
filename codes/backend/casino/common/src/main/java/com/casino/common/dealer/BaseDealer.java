package com.casino.common.dealer;

import java.util.UUID;

import com.casino.common.game.GameData;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.table.TableStatus;
import com.casino.common.table.structure.ICasinoTable;
import com.casino.common.table.timing.TimeController;

public interface BaseDealer extends TimeController {
     ICasinoTable getTable();

    <T extends ICasinoPlayer> void onPlayerArrival(T player);

    <T extends ICasinoPlayer> void onWatcherArrival(T player);

    int getPlayerTurnTime();

    GameData getGameData();

    void onPlayerTimeout(ICasinoPlayer player);

    default void updateCounterTime(Integer time) {
        getTable().updateCounterTime(time);
    }

    default Integer getCounterTime() {
        return getTable().getCounterTime();
    }

    default boolean isClockTicking() {
        return getTable().isClockTicking();
    }

    default void stopClock() {
        getTable().stopClock();
    }

    default UUID getTableId() {
        return getTable().getId();
    }

    default void onError(){
        getTable().stopClock();
        getTable().setStatus(TableStatus.ERROR);
    }
}
