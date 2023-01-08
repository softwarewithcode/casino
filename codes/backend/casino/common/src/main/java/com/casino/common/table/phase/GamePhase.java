package com.casino.common.table.phase;

public enum GamePhase {
	BET(true), BETS_COMPLETED(true), INSURE(true), PLAY(true), ROUND_COMPLETED(false), ERROR(false);

	private boolean running;

	private GamePhase(boolean round) {
		this.running = round;
	}

	public boolean isRoundRunning() {
		return this.running;
	}
}
