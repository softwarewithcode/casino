package com.casino.blackjack.game;

import com.casino.common.game.phase.GamePhase;
import com.casino.common.game.phase.PhasePath;

import java.util.ArrayList;
import java.util.List;

public class BlackjackPhasePathFactory {

	public static PhasePath buildBlackjackPath() {
		return buildBlackjackPhasePath();
	}

	private static PhasePath buildBlackjackPhasePath() {
		List<GamePhase> phases = new ArrayList<>();
		phases.add(BlackjackGamePhase.BET);
		phases.add(BlackjackGamePhase.BETS_COMPLETED);
		phases.add(BlackjackGamePhase.PLAY);
		return new PhasePath(phases);
	}
}
