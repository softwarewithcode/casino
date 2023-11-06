package com.casino.roulette.persistence;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.casino.roulette.bet.RouletteBet;

public record PlayerResult(
        List<RouletteBet> winningBets,
        List<RouletteBet> losingBets,
        BigDecimal totalBets,
        BigDecimal totalRemainingBets,
        BigDecimal totalWinnings,
        BigDecimal remainingBalance,
        UUID playerId) {
}
