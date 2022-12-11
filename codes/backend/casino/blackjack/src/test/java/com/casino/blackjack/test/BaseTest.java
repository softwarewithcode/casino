package com.casino.blackjack.test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import com.casino.common.table.Type;

public class BaseTest {
	protected static final BigDecimal MIN_BET = new BigDecimal("5.0");
	protected static final BigDecimal MAX_BET = new BigDecimal("100.0");
	protected static final Integer BET_ROUND_TIME_SECONDS = 2;
	protected static final Integer INSURANCE_ROUND_TIME_SECONDS = 3;
	protected static final Integer PLAYER_TIME_SECONDS = 4;
	protected static final long DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS = 2500l;
	protected static final Integer MIN_PLAYERS = 0;
	protected static final Integer MAX_PLAYERS = 7;
	protected static final Integer DEFAULT_SEAT_COUNT = 7;
	protected static final Type PUBLIC_TABLE_TYPE = Type.PUBLIC;

	protected void sleep(int i, ChronoUnit unit) {
		try {
			Thread.sleep(Duration.of(i, unit));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
