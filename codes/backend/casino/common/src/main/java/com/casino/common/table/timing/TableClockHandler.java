package com.casino.common.table.timing;

import com.casino.common.table.structure.CasinoTable;

public interface TableClockHandler {
    CasinoTable getTable();

    default void stopClock() {
        getTable().stopTiming();
    }

    default void updateCounterTime(Integer time) {
        getTable().updateCounterTime(time);
    }

    default Integer getCounterTime() {
        return getTable().getCounterTime();
    }

    default boolean isClockTicking() {
        return getTable().isClockTicking();
    }
}
