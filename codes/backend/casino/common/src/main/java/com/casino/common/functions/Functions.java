package com.casino.common.functions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.BiFunction;

public class Functions {
	public static final BiFunction<BigDecimal, BigDecimal, Boolean> isFirstMoreThanSecond = (x, y) -> x.compareTo(y) > 0;
	public static final BiFunction<BigDecimal, BigDecimal, Boolean> isFirstMoreOrEqualToSecond = (x, y) -> x.compareTo(y) >= 0;

	public static final BiFunction<Integer, Integer, Integer> getNextValueClockwise = (maxValue, currentValue) -> currentValue >= maxValue ? 0 : (currentValue + 1);
	public static final BiFunction<Integer, Integer, Integer> getNextValueCounterClockwise = (maxValue, currentValue) -> currentValue <= 0 ? maxValue : (currentValue - 1);
	public static final InBetweenFunction inBetween = (Integer lowest, Integer highest, Integer val) -> val > lowest && val < highest;

	public static boolean isFirstMoreOrEqualToSecond_(BigDecimal first, BigDecimal second) {
		return isFirstMoreOrEqualToSecond.apply(first, second);
	}

	public static BigDecimal transformToCasinoNumber(BigDecimal number) {
		if (number == null)
			return BigDecimal.ZERO.setScale(2,RoundingMode.DOWN);
		return number.setScale(2,RoundingMode.DOWN);
	}

	public final static BigDecimal calculateIncreaseAmount(BigDecimal additionalChipAttempt, BigDecimal maxAllowedAddition) {
		BigDecimal balanceIncrease;
		if (Functions.isFirstMoreOrEqualToSecond_(maxAllowedAddition, additionalChipAttempt))
			balanceIncrease = additionalChipAttempt;
		else
			balanceIncrease = maxAllowedAddition;
		return balanceIncrease;
	}
}
