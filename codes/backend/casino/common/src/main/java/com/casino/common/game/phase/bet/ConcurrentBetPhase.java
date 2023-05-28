package com.casino.common.game.phase.bet;

import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

//All players bet concurrently at the same time
public class ConcurrentBetPhase<T extends BetPhaser> extends TimerTask {
    private static final Logger LOGGER = Logger.getLogger(ConcurrentBetPhase.class.getName());
    private final T betPhaser;

    public ConcurrentBetPhase(T betPhaser) {
        this.betPhaser = betPhaser;
        betPhaser.updateCounterTime(betPhaser.getBetPhaseTime());
    }

    @Override
    public void run() {
        if (!betPhaser.isClockTicking()) {
            LOGGER.fine("BetPhaseClockTask, clock not ticking:");
            return;
        }
        if (betPhaser.shouldPrepareNewRound()) {
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