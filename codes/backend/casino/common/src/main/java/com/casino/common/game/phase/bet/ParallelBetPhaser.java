package com.casino.common.game.phase.bet;

import com.casino.common.table.timing.TableClockHandler;

//Related to games where players bet at the same time within time limits
public interface ParallelBetPhaser extends TableClockHandler {
    void onBetPhaseEnd();

    Integer getBetPhaseTime();

    boolean shouldRestartBetPhase();

    void reInitializeBetPhase();

    default void startParallelBetPhase(long initialDelay) {
        ParallelBetPhaser betPhaser = getTable().getDealer();
        ParallelBetPhase timerTask = new ParallelBetPhase<>(betPhaser);
        getTable().startTiming(timerTask, initialDelay);
    }
}
