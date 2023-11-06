package com.casino.roulette.game;

import java.util.ArrayList;
import java.util.List;

import com.casino.common.game.phase.GamePhase;
import com.casino.common.game.phase.PhasePath;

public class RoulettePhasePathFactory {
	public static PhasePath buildRoulettePhases() {
		List<GamePhase> phases = new ArrayList<>();
		phases.add(RouletteGamePhase.BET);
		phases.add(RouletteGamePhase.BETS_COMPLETED);
		phases.add(RouletteGamePhase.SPINNING);
		phases.add(RouletteGamePhase.ROUND_COMPLETED);
		return new PhasePath(phases);
	}
}
