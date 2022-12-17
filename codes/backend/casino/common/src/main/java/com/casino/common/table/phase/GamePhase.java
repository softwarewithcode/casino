package com.casino.common.table.phase;

public enum GamePhase {
	BET(true), BETS_COMPLETED(true), INSURE(true), PLAY(true), ROUND_COMPLETED(false), ERROR(false);

	private boolean onGoingRound;

	private GamePhase(boolean round) {
		this.onGoingRound = round;
	}

	public boolean isOnGoingRound() {
		return this.onGoingRound;
	}
}
