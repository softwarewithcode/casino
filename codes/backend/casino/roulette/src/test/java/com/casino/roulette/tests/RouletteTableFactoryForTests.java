package com.casino.roulette.tests;

import com.casino.common.game.Game;
import com.casino.common.language.Language;
import com.casino.common.ranges.Range;
import com.casino.common.table.TableData;
import com.casino.common.table.TableStatus;
import com.casino.common.table.TableThresholds;
import com.casino.common.table.structure.TableType;
import com.casino.roulette.export.RouletteTableAPI;
import com.casino.roulette.game.RouletteData;
import com.casino.roulette.game.RoulettePhasePathFactory;
import com.casino.roulette.table.RouletteTable_;

import java.math.BigDecimal;
import java.util.UUID;

//TODO remove this as duplicate
public class RouletteTableFactoryForTests {
    public static final Integer MIN_PLAYER_DEFAULT = 0;
    private static final Integer MAX_PLAYERS_DEFAULT = 7;
    private static final Integer SEAT_COUNT_DEFAULT = 7;
    private static final Integer DEFAULT_BETPHASE_TIME = 2;
    private static final long NEW_ROUND_DELAY = 500L;
    private static final Integer SINGLE_PLAYER = 1;
    private static final BigDecimal TEN_THOUSAND = new BigDecimal("10000");
    private static final Integer DEFAULT_HISTORY_SIZE = 10;
    public static final Long DEFAULT_SPIN_TIME_MILLIS = 5L;
    private static final Range<Integer> TABLE_NUMBERS = new Range<>(0, 36);

    public static RouletteTable_ createDefaultMultiplayerTable() {
        TableData tableInitData = createTableInitData(MAX_PLAYERS_DEFAULT, SEAT_COUNT_DEFAULT, TableType.MULTIPLAYER);
        Range<BigDecimal> betRange = new Range<>(new BigDecimal("5.0"), new BigDecimal("5000.0"));
        RouletteData rouletteInitData = createRouletteMultiplayerInitData(new BigDecimal("5.0"), betRange, NEW_ROUND_DELAY, DEFAULT_SPIN_TIME_MILLIS);
        return new RouletteTable_(tableInitData, rouletteInitData);
    }

    public static RouletteTable_ createTableWithSpinningTime(Long millis, TableType tableType) {
        TableData tableInitData = createTableInitData(MAX_PLAYERS_DEFAULT, SEAT_COUNT_DEFAULT, tableType);
        Range<BigDecimal> betRange = new Range<>(new BigDecimal("5.0"), new BigDecimal("5000.0"));
        RouletteData rouletteInitData = createRouletteMultiplayerInitData(new BigDecimal("5.0"), betRange, NEW_ROUND_DELAY, millis);
        return new RouletteTable_(tableInitData, rouletteInitData);
    }
    public static RouletteTable_ createMultiplayerTable(Integer seats) {
        TableData tableInitData = createTableInitData(seats, seats, TableType.MULTIPLAYER);
        Range<BigDecimal> betRange = new Range<>(new BigDecimal("5.0"), new BigDecimal("10.0"));
        RouletteData rouletteInitData = createRouletteMultiplayerInitData(new BigDecimal("5.0"), betRange,  NEW_ROUND_DELAY, DEFAULT_SPIN_TIME_MILLIS);
        return new RouletteTable_(tableInitData, rouletteInitData);
    }

    public static RouletteTable_ createDefaultSinglePlayerTable() {
        TableData tableInitData = createTableInitData(SINGLE_PLAYER, SINGLE_PLAYER, TableType.SINGLEPLAYER);
        Range<BigDecimal> betRange = new Range<>(new BigDecimal("5.0"), new BigDecimal("30.0"));
        RouletteData rouletteInitData = createRouletteSinglePlayerInitData(new BigDecimal("5.0"), betRange, 0);
        return new RouletteTable_(tableInitData, rouletteInitData);
    }

    public static RouletteTableAPI createHighRollerSinglePlayerTable() {
        TableData tableInitData = createTableInitData(SINGLE_PLAYER, SINGLE_PLAYER, TableType.SINGLEPLAYER);
        Range<BigDecimal> betRange = new Range<>(TEN_THOUSAND, new BigDecimal("50000"));
        RouletteData rouletteInitData = createRouletteSinglePlayerInitData(TEN_THOUSAND, betRange, 1);
        return new RouletteTable_(tableInitData, rouletteInitData);
    }

    private static RouletteData createRouletteMultiplayerInitData(BigDecimal minimumBuyIn, Range<BigDecimal> betRange,
                                                                  Long newRoundDelay,Long spinTimeMillis) {
        return new RouletteData(minimumBuyIn, betRange, RouletteTableFactoryForTests.DEFAULT_BETPHASE_TIME, 1, TABLE_NUMBERS, DEFAULT_HISTORY_SIZE, spinTimeMillis, newRoundDelay);
    }

    private static RouletteData createRouletteSinglePlayerInitData(BigDecimal minimumBuyIn, Range<BigDecimal> betRange, Integer skips) {
        return new RouletteData(minimumBuyIn, betRange, null, skips, TABLE_NUMBERS, DEFAULT_HISTORY_SIZE, DEFAULT_SPIN_TIME_MILLIS, null);
    }

    private static TableData createTableInitData(Integer maxPlayer, Integer seatCount, TableType tableType) {
        TableThresholds thresholds = new TableThresholds(RouletteTableFactoryForTests.MIN_PLAYER_DEFAULT, maxPlayer, seatCount);
        return new TableData(RoulettePhasePathFactory.buildRoulettePhases(), TableStatus.WAITING_PLAYERS, thresholds, UUID.randomUUID(), Language.ENGLISH, tableType, Game.ROULETTE);
    }
}
