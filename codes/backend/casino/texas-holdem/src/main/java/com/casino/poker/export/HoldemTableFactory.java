package com.casino.poker.export;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.casino.common.language.Language;
import com.casino.common.game.Game;
import com.casino.common.table.TableStatus;
import com.casino.common.table.TableData;
import com.casino.common.table.TableThresholds;
import com.casino.common.table.structure.TableType;
import com.casino.common.game.phase.GamePhase;
import com.casino.common.game.phase.PhasePath;
import com.casino.poker.bet.BetType;
import com.casino.poker.game.HoldemPhase;
import com.casino.poker.game.PokerData;
import com.casino.poker.table.HoldemTable;
import com.casino.poker.table.PokerTableType;

// There are no optional parameters for table building
public class HoldemTableFactory {
	private static final Integer DEFAULT_MIN_PLAYERS = 2;
	private static final Integer DEFAULT_MAX_PLAYERS = 6;
	private static final Integer DEFAULT_PLAYER_TIME = 20;
	private static final Integer DEFAULT_TIME_BANK = 30;
	private static final Integer DEFAULT_SEAT_COUNT = 6;
	private static final Integer DEFAULT_SKIP_COUNT = 3;
	private static final BigDecimal DEFAULT_RAKE_PERCENT = new BigDecimal("0.05");
	private static final BigDecimal DEFAULT_RAKE_CAP = new BigDecimal("20.0");
	private static final BigDecimal DEFAULT_ANTE = new BigDecimal("5.0");
	private static final BigDecimal DEFAULT_MIN_BUY_IN = new BigDecimal("200");
	private static final BigDecimal DEFAULT_MAX_BUY_IN = new BigDecimal("1000");
	private static final BigDecimal DEFAULT_SMALL_BLIND = new BigDecimal("5");
	private static final BigDecimal DEFAULT_BIG_BLIND = new BigDecimal("10");
	private static final Long DEFAULT_ROUND_DELAY = 5500L;

	public static NoLimitTexasHoldemAPI createDefaultTexasHoldemCashGameTable() {
		TableData tableData = createDefaultInitialTableData();
		PokerData pokerInitData = createDefaultPokerInitData();
		return new HoldemTable(tableData, pokerInitData);
	}

	public static NoLimitTexasHoldemAPI createDefaultTexasHoldemCashGameTableWithRakeCap(BigDecimal rake) {
		TableData tableData = createDefaultInitialTableData();
		PokerData pokerInitData = createDefaultPokerInitDataWithRake(rake);
		return new HoldemTable(tableData, pokerInitData);
	}

	// Contains no optional parameters
	private static PokerData createPokerInitData(PokerTableType tableType, Integer playerTime, Integer timeBank, Integer defaultSkipCount, BetType betType, BigDecimal minBuyIn, BigDecimal maxBuyIn, BigDecimal smallBlind,
			BigDecimal bigBlind, BigDecimal rakePercent, BigDecimal rakeCap, boolean antes, boolean straddle) {
		return new PokerData(tableType, playerTime, timeBank, defaultSkipCount, betType, minBuyIn, maxBuyIn, smallBlind, bigBlind, rakePercent, rakeCap, antes, DEFAULT_ANTE, straddle, DEFAULT_ROUND_DELAY);
	}

	private static TableData createTableData(PhasePath path, TableStatus status, TableThresholds tableThresholds, UUID tableId, Language language, TableType tableType, Game game) {
		return new TableData(path, status, tableThresholds, tableId, language, tableType, game);
	}

	private static PokerData createDefaultPokerInitData() {
		return createPokerInitData(PokerTableType.CASH, DEFAULT_PLAYER_TIME, DEFAULT_TIME_BANK, DEFAULT_SKIP_COUNT, BetType.NO_LIMIT, DEFAULT_MIN_BUY_IN, DEFAULT_MAX_BUY_IN, DEFAULT_SMALL_BLIND, DEFAULT_BIG_BLIND, DEFAULT_RAKE_PERCENT,
				DEFAULT_RAKE_CAP, false, false);
	}

	private static PokerData createDefaultPokerInitDataWithRake(BigDecimal rake) {
		return createPokerInitData(PokerTableType.CASH, DEFAULT_PLAYER_TIME, DEFAULT_TIME_BANK, DEFAULT_SKIP_COUNT, BetType.NO_LIMIT, DEFAULT_MIN_BUY_IN, DEFAULT_MAX_BUY_IN, DEFAULT_SMALL_BLIND, DEFAULT_BIG_BLIND, DEFAULT_RAKE_PERCENT,
				rake, false, false);
	}

	private static TableData createDefaultInitialTableData() {
		List<GamePhase> holdemPhase = new ArrayList<>();
		holdemPhase.add(HoldemPhase.PRE_FLOP);
		holdemPhase.add(HoldemPhase.FLOP);
		holdemPhase.add(HoldemPhase.TURN);
		holdemPhase.add(HoldemPhase.RIVER);
		holdemPhase.add(HoldemPhase.COMPLETE_ROUND);
		PhasePath path = new PhasePath(holdemPhase);
		TableStatus status = TableStatus.WAITING_PLAYERS;
		TableThresholds tableThresholds = new TableThresholds(DEFAULT_MIN_PLAYERS, DEFAULT_MAX_PLAYERS, DEFAULT_SEAT_COUNT);
		UUID tableId = UUID.randomUUID();
		Language language = Language.ENGLISH;
		TableType tableType = TableType.MULTIPLAYER;
		Game game = Game.TEXAS_HOLDEM;
		return createTableData(path, status, tableThresholds, tableId, language, tableType, game);
	}

}
