package com.casino.poker.game;

import java.math.BigDecimal;

import com.casino.common.game.Game;
import com.casino.common.game.GameData;
import com.casino.poker.bet.BetType;
import com.casino.poker.table.PokerTableType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(value = { "maxBet" })
public record PokerData(PokerTableType type,
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
                            boolean ante,//duplicate information -> anteAmount==null?
                            BigDecimal anteAmount,
                            boolean straddle,
                            Long newRoundDelay) implements GameData {

    @Override
    public Integer getMaxSkips() {
        return allowedSkips;
    }

    @Override
    public BigDecimal getMaxBet() {
       throw new UnsupportedOperationException(" should not be called in no-limit holdem. method required by interface ");
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
	@Override
	public Game getGame() {
		// TODO Auto-generated method stub
		return Game.TEXAS_HOLDEM;
	}
}