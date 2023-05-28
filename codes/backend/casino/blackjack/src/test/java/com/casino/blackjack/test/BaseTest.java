package com.casino.blackjack.test;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;

import com.casino.common.language.Language;
import com.casino.common.message.Mapper;
import com.casino.common.game.Game;
import com.casino.common.table.TableStatus;
import com.casino.common.table.TableData;
import com.casino.common.table.TableThresholds;
import com.casino.common.table.structure.TableType;
import com.casino.blackjack.game.BlackjackInitData;
import com.casino.blackjack.game.BlackjackPhasePathFactory;
import com.casino.common.user.Bridge;

public class BaseTest {
	protected static final BigDecimal MIN_BET = new BigDecimal("5.0");
	protected static final BigDecimal MIN_BUYIN = new BigDecimal("5.0");
	protected static final BigDecimal MAX_BET = new BigDecimal("100.0");
	protected static final Integer BET_ROUND_TIME_SECONDS = 2;
	protected static final Integer INSURANCE_ROUND_TIME_SECONDS = 3;
	protected static final Integer PLAYER_TIME_SECONDS = 4;
	protected static final long DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS = 2500l;
	protected static final Integer MIN_PLAYERS = 0;
	protected static final Integer MAX_PLAYERS = 7;
	protected static final Integer DEFAULT_SEAT_COUNT = 7;
	protected static final Integer DEFAULT_ALLOWED_SIT_OUT_ROUNDS = 1;
	protected static final TableType PUBLIC_TABLE_TYPE = TableType.MULTIPLAYER;
	protected static final int ONE_UNIT = 1;
	protected static final BlackjackInitData blackjackInitData = new BlackjackInitData(MIN_BUYIN, MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DEFAULT_ALLOWED_SIT_OUT_ROUNDS,
			DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS);
	protected Bridge bridge;
	protected Bridge bridge2;
	protected Bridge bridge3;

	@BeforeAll
	public static void setup() {
		System.getProperties().setProperty(Mapper.JUNIT_RUNNER, "true");
	}

	protected TableThresholds getDefaultThresholds() {
		return new TableThresholds(MIN_PLAYERS, MAX_PLAYERS, DEFAULT_SEAT_COUNT);
	}


	protected TableThresholds getThresholdsWithPlayersMinAndMax(Integer minPlayers, Integer maxPlayers) {
		return new TableThresholds(MIN_PLAYERS, MAX_PLAYERS, DEFAULT_SEAT_COUNT);
	}

	protected TableData getDefaultTableInitData() {
		return new TableData(BlackjackPhasePathFactory.buildBlackjackPath(), TableStatus.WAITING_PLAYERS, getDefaultThresholds(), UUID.randomUUID(), Language.ENGLISH, TableType.MULTIPLAYER, Game.BLACKJACK);
	}

	protected TableData getDefaultSinglePlayerTableInitData() {
		return new TableData(BlackjackPhasePathFactory.buildBlackjackPath(), TableStatus.WAITING_PLAYERS, getDefaultThresholds(), UUID.randomUUID(), Language.ENGLISH, TableType.SINGLE_PLAYER, Game.BLACKJACK);
	}

	protected TableData getDefaultTableInitDataWithThresholds(TableThresholds thresholds) {
		return new TableData(BlackjackPhasePathFactory.buildBlackjackPath(), TableStatus.WAITING_PLAYERS, thresholds, UUID.randomUUID(), Language.ENGLISH, TableType.MULTIPLAYER, Game.BLACKJACK);
	}


//	protected TableInitData getDefaultTableInitDataWithPlayersMinAndMax(Integer minPlayers, Integer maxPlayers) {
//		return new TableInitData(PhasePathFactory.buildBlackjackPath(), Status.WAITING_PLAYERS, getThresholdsWithPlayersMinAndMax(minPlayers, maxPlayers), UUID.randomUUID(), Language.ENGLISH, Type.MULTIPLAYER, Game.BLACKJACK);
//	}
	
	protected BlackjackInitData createBlackjackInitData(BigDecimal minimumBuyIn, BigDecimal minimumBet, BigDecimal maximumBet, Integer betPhaseTime, Integer insurancePhaseTime, Integer playerTime, Integer skips, Long newRoundDelay) {
		return new BlackjackInitData(minimumBuyIn, minimumBet, maximumBet, betPhaseTime, insurancePhaseTime, playerTime, skips, newRoundDelay);
	}

	protected void sleep(int i, Object unit) {
		try {
			Thread.sleep((Integer) i * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
