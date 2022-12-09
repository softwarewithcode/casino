package com.casino.common.table;

import java.math.BigDecimal;

public record Thresholds(
		BigDecimal minimumBet, 
		BigDecimal	maximumBet, 
		Integer betPhaseTime,
		Integer secondPhaseTime,
		Integer playerHandTime,
		Integer phaseDelay,
		Integer minPlayers,
		Integer maxPlayers,
		Integer seatCount,
		Type tableType) {
}
