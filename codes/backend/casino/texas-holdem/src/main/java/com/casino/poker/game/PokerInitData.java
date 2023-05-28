package com.casino.poker.game;

import com.casino.common.game.GameData;
import com.casino.poker.bet.BetType;
import com.casino.poker.table.PokerTableType;

import java.math.BigDecimal;

public record PokerInitData(PokerTableType type,
                            Integer playerTime,
                            Integer timeBank,
                            Integer allowedSkips,
                            BetType betType,
                            BigDecimal minBuyIn,
                            BigDecimal maxBuyIn,
                            BigDecimal smallBlind,
                            BigDecimal bigBlind,
                            BigDecimal rakePercent,
                            BigDecimal rakeCap,
                            boolean ante,
                            BigDecimal anteAmount,
                            boolean straddle,
                            Long newRoundDelay) implements GameData {

    @Override
    public Integer getMaxSkips() {
        return allowedSkips;
    }

    @Override
    public BigDecimal getMaxBet() {
        // maximum bet is not defined in Poker as constant. Method is no use here
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getMinBet() {
        return bigBlind;
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
        return playerTime;
    }

    @Override
    public Integer getExtraTime() {
        return timeBank;
    }

}