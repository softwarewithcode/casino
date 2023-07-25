package com.casino.common.game.phase.bet;

import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

//All players can bet parallel at the same time
public class ParallelBetPhase<T extends ParallelBetPhaser> extends TimerTask {
    private static final Logger LOGGER = Logger.getLogger(ParallelBetPhase.class.getName());
    private final T betPhaser;

    public ParallelBetPhase(T betPhaser) {
        this.betPhaser = betPhaser;
        betPhaser.updateCounterTime(betPhaser.getBetPhaseTime());
    }

    @Override
    public void run() {
        if (!betPhaser.isClockTicking()) {
            LOGGER.fine("BetPhaseClockTask, clock not ticking:");
            return;
        }
        if (betPhaser.shouldPrepareBetPhase()) {
            LOGGER.info("BetPhaseClockTask, clear previous round " + betPhaser);
            betPhaser.prepareBetPhase();
        }
        int counterValue = betPhaser.getCounterTime();
        counterValue--;
        betPhaser.updateCounterTime(counterValue);
        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.fine("BetPhaseClockTask, secondsLeft:" + counterValue + " in table:" + betPhaser.getTableId());
        if (counterValue == 0) {
            betPhaser.stopClock();
            betPhaser.onBetPhaseEnd();
        }
    }

}