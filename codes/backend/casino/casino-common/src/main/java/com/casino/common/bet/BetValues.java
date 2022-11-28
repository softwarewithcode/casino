package com.casino.common.bet;

import java.math.BigDecimal;

public record BetValues(BigDecimal minimumBet, BigDecimal maximumBet, Integer betRoundTime, Integer betTime) {

}
