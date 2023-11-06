package com.casino.blackjack.game;

import com.casino.common.functions.Functions;
import com.casino.common.game.Game;
import com.casino.common.game.GameData;

import java.math.BigDecimal;

public record BlackjackData(
        BigDecimal minimumBuyIn,
        BigDecimal minBet,
        BigDecimal maxBet,
        Integer betPhaseTime,
        Integer insurancePhaseTime,
        Integer playerTime,
        Integer skips,
        Long newRoundDelay) implements GameData {
    // compact constructor
    public BlackjackData {
        if (Functions.isFirstMoreThanSecond.apply(BigDecimal.ZERO, minBet))
            throw new IllegalArgumentException("Minimum bet cannot be less than zero");
        if (Functions.isFirstMoreThanSecond.apply(minBet, maxBet))
            throw new IllegalArgumentException("maximum bet cannot be less than minimum bet");
        if (Functions.isFirstMoreThanSecond.apply(minimumBuyIn, minBet))
            throw new IllegalArgumentException("minimumBuyIn cannot be less than minimum bet");
    }

    @Override
    public BigDecimal getMaxBet() {
        return maxBet;
    }

    @Override
    public BigDecimal getMinBet() {
        return minBet;
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

	@Override
	public Game getGame() {
		// TODO Auto-generated method stub
		return Game.BLACKJACK;
	}

}
