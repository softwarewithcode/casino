package com.casino.roulette.bet;

import com.casino.roulette.export.BetType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.UUID;

@JsonIgnoreProperties("id")
public record BetData(UUID id, BigDecimal amount, BetType betType, Integer position) {

}
