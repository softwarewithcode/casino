package com.casino.roulette.game;

import com.casino.common.functions.Functions;
import com.casino.common.game.Game;
import com.casino.common.game.GameData;
import com.casino.common.ranges.Range;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.math.BigDecimal;

@JsonInclude(Include.NON_NULL)
public record RouletteData(BigDecimal minBuyIn, // betRange min would cover this
                           Range<BigDecimal> betRange,
                           Integer betPhaseTime,
                           Integer allowedSkips,
                           Range<Integer> tableNumbers,
                           Integer historySize,
                           Long spinTimeMillis,
                           Long newRoundDelay)
        implements GameData {
    public RouletteData {
        if (Functions.isFirstMoreThanSecond.apply(BigDecimal.ZERO, betRange.min()))
            throw new IllegalArgumentException("Minimum bet cannot be less than zero");
        if (Functions.isFirstMoreThanSecond.apply(betRange.min(), betRange.max()))
            throw new IllegalArgumentException("maximum bet cannot be less than minimum bet");
        if (!betRange.min().equals(minBuyIn))
            throw new IllegalArgumentException("MinBet should equal minBuyIn. MinBuyIn= " + minBuyIn + " minBet=" + betRange.min());
        if (historySize < 0 || historySize > 100)
            throw new IllegalArgumentException("HistorySize not correct was " + historySize);
    }

    @Override
    public Integer getMaxSkips() {
        return allowedSkips;
    }

    @Override
    public BigDecimal getMaxBet() {
        return betRange.max();
    }

    @Override
    public BigDecimal getMinBet() {
        return betRange.min();
    }

    @Override
    public BigDecimal getMinBuyIn() {
        return minBuyIn;
    }

    @Override
    public Long getRoundDelay() {
        return newRoundDelay;
    }

    @Override
    public Integer getPlayerTime() {
        return 0;
    }

    @Override
    public Integer getExtraTime() {
        return 0;
    }

    @Override
    public Game getGame() {
        return Game.ROULETTE;
    }
}
