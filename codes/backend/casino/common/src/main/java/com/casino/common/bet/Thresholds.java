package com.casino.common.bet;

import java.math.BigDecimal;

import com.casino.common.table.Type;

public record Thresholds(
		BigDecimal minimumBet, 
		BigDecimal	maximumBet, 
		Integer betPhaseTime,
		Integer secondPhaseTime,
		Integer playerActTime,
		Integer phaseDelay,
		Integer minPlayers,
		Integer maxPlayers,
		Integer seatCount,
		Type tableType) {
}
