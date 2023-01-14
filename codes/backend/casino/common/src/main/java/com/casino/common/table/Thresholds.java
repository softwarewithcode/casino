package com.casino.common.table;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record Thresholds(
		BigDecimal minimumBet, 
		BigDecimal maximumBet, 
		Integer betPhaseTime, 
		@JsonIgnore Integer secondPhaseTime, 
		Integer playerTime, 
		Long phaseDelay, 
		Integer minPlayers, 
		Integer maxPlayers, 
		Integer seatCount,
		Integer allowedBetRoundSkips
		) 
{
	// compact constructor
	public Thresholds {
		if (BigDecimal.ZERO.compareTo(minimumBet) == 1)
			throw new IllegalArgumentException("Minimum bet cannot be less than zero");
		if (minimumBet.compareTo(maximumBet) == 1)
			throw new IllegalArgumentException("maximum bet cannot be less than minimum bet");
	}
}
