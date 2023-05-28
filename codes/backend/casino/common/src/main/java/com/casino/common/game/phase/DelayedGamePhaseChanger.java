package com.casino.common.game.phase;

import java.util.concurrent.TimeUnit;

public interface DelayedGamePhaseChanger {
    void executeWithDelay(GamePhaser phaser, long delay, TimeUnit timeUnit);
}
