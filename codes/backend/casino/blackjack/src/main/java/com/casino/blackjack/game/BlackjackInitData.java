package com.casino.blackjack.game;

import java.math.BigDecimal;

import com.casino.common.functions.Functions;
import com.casino.common.game.GameData;

public record BlackjackInitData(
		BigDecimal minimumBuyIn,
		BigDecimal minimumBet,
		BigDecimal maximumBet,
		Integer betPhaseTime,
		Integer insurancePhaseTime,
		Integer playerTime,
		Integer skips,
		Long newRoundDelay) implements GameData {
	// compact constructor
	public BlackjackInitData {
		if (Functions.isFirstMoreThanSecond.apply(BigDecimal.ZERO, minimumBet))
			throw new IllegalArgumentException("Minimum bet cannot be less than zero");
		if (Functions.isFirstMoreThanSecond.apply(minimumBet, maximumBet))
			throw new IllegalArgumentException("maximum bet cannot be less than minimum bet");
		if (Functions.isFirstMoreThanSecond.apply(minimumBuyIn, minimumBet))
			throw new IllegalArgumentException("minimumBuyIn cannot be less than minimum bet");
	}

	@Override
	public BigDecimal getMaxBet() {
		return maximumBet;
	}

	@Override
	public BigDecimal getMinBet() {
		return minimumBet;
	}

	@Override
	public Integer getMaxSkips() {
		return skips;
	}

	@Override
	public BigDecimal getMinBuyIn() {
		return minimumBuyIn;
	}

	@Override
	public Long getRoundDelay() {
		return newRoundDelay;
	}

	@Override
	public Integer getPlayerTime() {
		return playerTime;
	}

	@Override
	public Integer getExtraTime() {
		return 0;
	}

}
