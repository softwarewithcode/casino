package com.casino.common.bet;

import java.math.BigDecimal;

public record BetThresholds(BigDecimal minimumBet, BigDecimal maximumBet, Integer betRoundTime, Integer betTime, Integer initialBetRoundDelay) {

}
