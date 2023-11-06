package com.casino.roulette.game;

import com.casino.common.game.phase.GamePhase;

public enum RouletteGamePhase implements GamePhase {
    BET(true), BETS_COMPLETED(true), SPINNING(true), ROUND_COMPLETED(false), ERROR(false);

    private final boolean running;

    RouletteGamePhase(boolean round) {
        this.running = round;
    }

    @Override
    public boolean isGameRunning() {
        return running;
    }

    @Override
    public boolean usesTimeBank() {
        return false;
    }
}
