package com.casino.common.game.phase.bet;

import com.casino.common.table.timing.TimeController;

//Related to games where players bet at the same time within time limits
public interface BetPhaser extends TimeController {
    void onBetPhaseEnd();

    Integer getBetPhaseTime();

    boolean shouldPrepareNewRound();
    void prepareBetPhase();
}
