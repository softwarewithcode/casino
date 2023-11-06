package com.casino.common.bet;

import java.math.BigDecimal;

import com.casino.common.functions.Functions;

public record BetRange(BigDecimal min, BigDecimal max) {

	public BetRange {
		if (Functions.isFirstMoreThanSecond.apply(BigDecimal.ZERO, min))
			throw new IllegalArgumentException("minimum cannot be less than zero. was:" + min);
		if (Functions.isFirstMoreThanSecond.apply(min, max))
			throw new IllegalArgumentException("maximum:" + max + " cannot be less than minimum:" + min);
	}

	public boolean isInRange(BigDecimal amount) {
		if (amount == null)
			return false;
		return Functions.isFirstMoreOrEqualToSecond.apply(amount, min) && Functions.isFirstMoreOrEqualToSecond.apply(max, amount);
	}
}
