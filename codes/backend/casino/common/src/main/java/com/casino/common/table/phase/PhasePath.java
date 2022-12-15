package com.casino.common.table.phase;

import java.util.List;

/*
 *
 * When table is exposed to public it can receive any data at any time to any exposed interface.
 * PhasePath can be used to tell what inputs are expected in current game phase.
 * For example it should not be possible to change the bet after cards have been dealed.
 *
 */
public class PhasePath {
	private final List<GamePhase> phases;
	private final boolean around;
	private volatile GamePhase currentPhase;

	public PhasePath(List<GamePhase> phases, boolean around) {
		if (phases == null || phases.size() < 2)
			throw new IllegalArgumentException("Not enough phases");
		this.phases = phases;
		this.around = around;
		this.currentPhase = phases.get(0);
	}

	public GamePhase getPhase() {
		return currentPhase;
	}

	public void setCurrentPhase(GamePhase currentPhase) {
		this.currentPhase = currentPhase;
	}

	public List<GamePhase> getPhases() {
		return phases;
	}

	public boolean isAround() {
		return around;
	}
}
