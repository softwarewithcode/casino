package com.casino.common.game.phase.bet;

import com.casino.common.table.timing.TimeController;

//Related to games where players bet at the same time within time limits
public interface ParallelBetPhaser extends TimeController {
    void onBetPhaseEnd();

    Integer getBetPhaseTime();

    boolean shouldPrepareBetPhase();
    void prepareBetPhase();
}
