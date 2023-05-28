package com.casino.poker.showdown;

import com.casino.common.game.phase.DelayedGamePhaseChanger;
import com.casino.common.game.phase.GamePhaser;
import com.casino.poker.game.HoldemPhase;

import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ShowdownHandler implements DelayedGamePhaseChanger {
    private static final Logger LOGGER = Logger.getLogger(ShowdownHandler.class.getName());
    private static final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void executeWithDelay(GamePhaser phaser, long delay, TimeUnit timeUnit) {
        while (phaser.getTable().getGamePhase() != HoldemPhase.RIVER) {
            try {
                ScheduledFuture<?> sf = scheduledExecutorService.schedule(() -> phaser.prepareNextGamePhase(), delay, timeUnit);
                sf.get();
            } catch (InterruptedException e) {
                LOGGER.log(Level.INFO, "Showdown wait was interrupted. Continuing immediately in table:" + phaser.getTable().getId() + " reason:", e);
            } catch (ExecutionException e) {
                LOGGER.log(Level.INFO, "Showdown ExecutionException. Continuing immediately in table:" + phaser.getTable().getId() + " reason:", e);
            }
        }
    }
}
