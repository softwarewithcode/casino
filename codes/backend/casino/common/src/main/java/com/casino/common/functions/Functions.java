package com.casino.common.functions;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.BiFunction;

import com.casino.common.action.PlayerAction;
import com.casino.common.validation.Verifier;

public class Functions {
	public static final BiFunction<BigDecimal, BigDecimal, Boolean> isFirstMoreThanSecond = (x, y) -> x.compareTo(y) > 0;
	public static final BiFunction<BigDecimal, BigDecimal, Boolean> isFirstMoreOrEqualToSecond = (x, y) -> x.compareTo(y) >= 0;

	public static final BiFunction<Integer, Integer, Integer> getNextValueClockwise = (maxValue, currentValue) -> currentValue >= maxValue ? 0 : (currentValue + 1);
	public static final BiFunction<Integer, Integer, Integer> getNextValueCounterClockwise = (maxValue, currentValue) -> currentValue <= 0 ? maxValue : (currentValue - 1);

	public static boolean isFirstMoreOrEqualToSecond_(BigDecimal first, BigDecimal second) {
		return isFirstMoreOrEqualToSecond.apply(first, second);
	}

	public static boolean isFirstMoreThanSecond_(BigDecimal first, BigDecimal second) {
		Verifier.verifyNotNull(first);
		return isFirstMoreThanSecond.apply(first, second);
	}

	public static BigDecimal calculateSum(List<BigDecimal> bigDecimals) {
		if (bigDecimals == null || bigDecimals.isEmpty())
			return BigDecimal.ZERO;
		return bigDecimals.parallelStream().reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public static boolean containsAction(List<? extends PlayerAction> allowedActions, PlayerAction actionForCheck) {
		if (allowedActions == null)
			return false;
		return allowedActions.contains(actionForCheck);
	}

	public static BigDecimal calculateIncreaseAmount(BigDecimal additionalChipAttempt, BigDecimal maxAllowedAddition) {
		BigDecimal balanceIncrease;
		if (Functions.isFirstMoreOrEqualToSecond_(maxAllowedAddition, additionalChipAttempt))
			balanceIncrease = additionalChipAttempt;
		else
			balanceIncrease = maxAllowedAddition;
		return balanceIncrease;
	}
}
