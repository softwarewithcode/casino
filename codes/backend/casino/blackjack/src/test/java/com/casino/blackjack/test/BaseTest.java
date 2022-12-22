package com.casino.blackjack.test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;

import com.casino.blackjack.message.Mapper;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.language.Language;
import com.casino.common.table.Game;
import com.casino.common.table.TableInitData;
import com.casino.common.table.Thresholds;
import com.casino.common.table.Type;
import com.casino.common.user.Bridge;

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
	protected Bridge bridge;
	protected Bridge bridge2;
	protected Bridge bridge3;

	@BeforeAll
	public static void setup() {
		System.out.println("before all test -> setting skip serialization parameter ");
		System.getProperties().setProperty(Mapper.JUNIT_RUNNER, "true");
	}

	private BlackjackTable defaultTable;

	protected Thresholds getDefaultThresholds() {
		return new Thresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS, MIN_PLAYERS, MAX_PLAYERS, DEFAULT_SEAT_COUNT);
	}

	protected Thresholds getThresholdsWithMinBets(BigDecimal minbet, BigDecimal maxBet) {
		return new Thresholds(minbet, maxBet, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS, MIN_PLAYERS, MAX_PLAYERS, DEFAULT_SEAT_COUNT);
	}

	protected Thresholds getThresholdsWithPlayersMinAndMax(Integer minPlayers, Integer maxPlayers) {
		return new Thresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS, minPlayers, maxPlayers, maxPlayers);
	}

	protected TableInitData getDefaultTableInitData() {
		return new TableInitData(getDefaultThresholds(), UUID.randomUUID(), Language.ENGLISH, Type.PUBLIC, Game.BLACKJACK);
	}

	protected TableInitData getDefaultTableInitDataWithThresholds(Thresholds thresholds) {
		return new TableInitData(thresholds, UUID.randomUUID(), Language.ENGLISH, Type.PUBLIC, Game.BLACKJACK);
	}

	protected TableInitData getDefaultTableInitDataWithBets(BigDecimal minBet, BigDecimal maxBet) {
		return new TableInitData(getThresholdsWithMinBets(minBet, maxBet), UUID.randomUUID(), Language.ENGLISH, Type.PUBLIC, Game.BLACKJACK);
	}

	protected TableInitData getDefaultTableInitDataWithPlayersMinAndMax(Integer minPlayers, Integer maxPlayers) {
		return new TableInitData(getThresholdsWithPlayersMinAndMax(minPlayers, maxPlayers), UUID.randomUUID(), Language.ENGLISH, Type.PUBLIC, Game.BLACKJACK);
	}

	protected void sleep(int i, ChronoUnit unit) {
		try {
			Thread.sleep(Duration.of(i, unit));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
