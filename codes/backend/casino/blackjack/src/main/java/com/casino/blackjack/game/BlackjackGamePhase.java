package com.casino.blackjack.game;

import com.casino.common.game.phase.GamePhase;

public enum BlackjackGamePhase implements GamePhase {
	BET(true), BETS_COMPLETED(true), INSURE(true), PLAY(true), ROUND_COMPLETED(false), ERROR(false);

	private final boolean running;

	BlackjackGamePhase(boolean round) {
		this.running = round;
	}

	@Override
	public boolean isGameRunning() {
		return this.running;
	}

	@Override
	public boolean usesTimeBank() {
		return false;
	}
}
