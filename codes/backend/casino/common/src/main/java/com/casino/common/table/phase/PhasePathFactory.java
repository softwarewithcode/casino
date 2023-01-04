package com.casino.common.table.phase;

import java.util.ArrayList;
import java.util.List;

public class PhasePathFactory {

	public static PhasePath buildBlackjackPath() {
		return buildBlackjackPhasePath();
	}

	private static PhasePath buildBlackjackPhasePath() {
		List<GamePhase> phases = new ArrayList<>();
		phases.add(GamePhase.BET);
		phases.add(GamePhase.BETS_COMPLETED);
		phases.add(GamePhase.PLAY);
		return new PhasePath(phases);
	}
}
