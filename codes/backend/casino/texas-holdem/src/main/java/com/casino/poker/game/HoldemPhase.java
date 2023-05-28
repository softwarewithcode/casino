package com.casino.poker.game;

import com.casino.common.game.phase.GamePhase;

public enum HoldemPhase implements GamePhase {
	PRE_FLOP(false, true), 
	FLOP(true, true), 
	TURN(true, true),
	RIVER(true, true),
	COMPLETE_ROUND(false, false),
	ROUND_COMPLETED(false, false),
	ERROR(false, false);

	private final boolean usesTimeBank;
	private final boolean running;

	HoldemPhase(boolean useTimeBank, boolean running) {
		this.usesTimeBank = useTimeBank;
		this.running = running;
	}

	@Override
	public boolean usesTimeBank() {
		return usesTimeBank;
	}

	@Override
	public boolean isRunning() {
		return running;
	}
}
