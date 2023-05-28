package com.casino.common.game;

import java.math.BigDecimal;

public interface GameData {

	Integer getMaxSkips();

	BigDecimal getMaxBet();

	BigDecimal getMinBet();

	BigDecimal getMinBuyIn();

	Long getRoundDelay();

	Integer getPlayerTime();

	Integer getExtraTime();

}
