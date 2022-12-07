package com.casino.blackjack.test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class BaseTest {
	protected static final BigDecimal MIN_BET = new BigDecimal("5.0");
	protected static final BigDecimal MAX_BET = new BigDecimal("100.0");
	protected static final Integer BET_ROUND_TIME_SECONDS = 2;
	protected static final Integer INSURANCE_ROUND_TIME_SECONDS = 3;
	protected static final Integer PLAYER_TIME = 10;
	protected static final Integer INITIAL_DELAY = 0;

	protected void sleep(int i, ChronoUnit unit) {
		try {
			Thread.sleep(Duration.of(i, unit));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
