package com.casino.blackjack.export;

import java.math.BigDecimal;
import java.util.UUID;

import com.casino.blackjack.game.BlackjackInitData;
import com.casino.blackjack.game.BlackjackPhasePathFactory;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.game.Game;
import com.casino.common.language.Language;
import com.casino.common.table.TableData;
import com.casino.common.table.TableStatus;
import com.casino.common.table.TableThresholds;
import com.casino.common.table.structure.TableType;

public class BlackjackTableFactory {
	private static final Integer MIN_PLAYER_DEFAULT = 0;
	private static final Integer MAX_PLAYERS_DEFAULT = 7;
	private static final Integer SEAT_COUNT_DEFAULT = 7;

	public static BlackjackAPI createDefaultMultiplayerTable() {
		TableData tableInitData = createTableInitData(MIN_PLAYER_DEFAULT, MAX_PLAYERS_DEFAULT, SEAT_COUNT_DEFAULT, TableType.MULTIPLAYER);
		BlackjackInitData blackjackInitData = createBlackjackInitData(new BigDecimal("5.0"), new BigDecimal("5.0"), new BigDecimal("10.0"), 10, 10, 10, 2, 5000l);
		BlackjackTable temp1 = new BlackjackTable(tableInitData, blackjackInitData);
		return temp1;
	}

	public static BlackjackAPI createDefaultSinglePlayerTable() {
		TableData tableInitData = createTableInitData(MIN_PLAYER_DEFAULT, MAX_PLAYERS_DEFAULT, SEAT_COUNT_DEFAULT, TableType.MULTIPLAYER);
		BlackjackInitData blackjackInitData = createBlackjackInitData(new BigDecimal("5.0"), new BigDecimal("5.0"), new BigDecimal("10.0"), 10, 10, 10, 2, 5000l);
		BlackjackTable temp1 = new BlackjackTable(tableInitData, blackjackInitData);
		return temp1;
	}

	public static BlackjackAPI createMultiPlayerTable(Integer minPlayers, Integer maxPlayers, BigDecimal minimumBuyIn, BigDecimal minimumBet, BigDecimal maximumBet, Integer betPhaseTime, Integer insurancePhaseTime, Integer playerTime,
			Integer skips, Long newRoundDelay) {
		TableData tableInitData = createTableInitData(minPlayers, maxPlayers, maxPlayers, TableType.MULTIPLAYER);
		BlackjackInitData blackjackInitData = createBlackjackInitData(minimumBuyIn, minimumBet, maximumBet, betPhaseTime, insurancePhaseTime, playerTime, skips, newRoundDelay);
		return new BlackjackTable(tableInitData, blackjackInitData);
	}

	private static BlackjackInitData createBlackjackInitData(BigDecimal minimumBuyIn, BigDecimal minimumBet, BigDecimal maximumBet, Integer betPhaseTime, Integer insurancePhaseTime, Integer playerTime, Integer skips, Long newRoundDelay) {
		return new BlackjackInitData(minimumBuyIn, minimumBet, maximumBet, betPhaseTime, insurancePhaseTime, playerTime, skips, newRoundDelay);
	}

	private static TableData createTableInitData(Integer minPlayer, Integer maxPlayer, Integer seatCount, TableType tableType) {
		TableThresholds thresholds = new TableThresholds(minPlayer, maxPlayer, seatCount);
		TableData tableInitData = new TableData(BlackjackPhasePathFactory.buildBlackjackPath(), TableStatus.WAITING_PLAYERS, thresholds, UUID.randomUUID(), Language.ENGLISH, tableType, Game.BLACKJACK);
		return tableInitData;
	}
}
