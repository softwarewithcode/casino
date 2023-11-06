package com.casino.roulette.export;

import com.casino.common.game.Game;
import com.casino.common.language.Language;
import com.casino.common.ranges.Range;
import com.casino.common.table.TableData;
import com.casino.common.table.TableStatus;
import com.casino.common.table.TableThresholds;
import com.casino.common.table.structure.TableType;
import com.casino.roulette.game.RouletteData;
import com.casino.roulette.game.RoulettePhasePathFactory;
import com.casino.roulette.table.RouletteTable_;

import java.math.BigDecimal;
import java.util.UUID;

public class RouletteTableFactory {
    private static final Integer MIN_PLAYER_DEFAULT = 0;
    private static final Integer MAX_PLAYERS_DEFAULT = 7;
    private static final Integer SEAT_COUNT_DEFAULT = 7;
    private static final Integer DEFAULT_BETPHASE_TIME = 15;
    private static final long DEFAULT_ROUND_DELAY = 1000L;
    private static final Integer SINGLE_PLAYER = 1;
    private static final Integer DEFAULT_HISTORY_SIZE = 10;
    private static final Long DEFAULT_SPIN_TIME_MILLIS = 6000L;
    private static final BigDecimal ONE = new BigDecimal("1.00");
    private static final Range<Integer> TABLE_NUMBERS = new Range<>(0, 36);

    public static RouletteTableAPI createDefaultMultiplayerTable() {
        TableData tableInitData = createTableInitData(MAX_PLAYERS_DEFAULT, SEAT_COUNT_DEFAULT, TableType.MULTIPLAYER);
        Range<BigDecimal> betRange = new Range<>(new BigDecimal("5.0"), new BigDecimal("5000.0"));
        RouletteData rouletteInitData = createRouletteMultiplayerInitData(new BigDecimal("5.0"), betRange, RouletteTableFactory.DEFAULT_ROUND_DELAY);
        return new RouletteTable_(tableInitData, rouletteInitData);
    }

    public static RouletteTableAPI createDefaultSinglePlayerTable() {
        TableData tableInitData = createTableInitData(SINGLE_PLAYER, SINGLE_PLAYER, TableType.SINGLEPLAYER);
        Range<BigDecimal> betRange = new Range<>(new BigDecimal("5.0"), new BigDecimal("10.0"));
        RouletteData rouletteInitData = createRouletteSinglePlayerInitData(new BigDecimal("5.0"), betRange);
        return new RouletteTable_(tableInitData, rouletteInitData);
    }

    public static RouletteTableAPI createHighRollerSinglePlayerTable() {
        TableData tableInitData = createTableInitData(SINGLE_PLAYER, SINGLE_PLAYER, TableType.SINGLEPLAYER);
        Range<BigDecimal> betRange = new Range<>(ONE, new BigDecimal("50000"));
        RouletteData rouletteInitData = createRouletteSinglePlayerInitData(ONE, betRange);
        return new RouletteTable_(tableInitData, rouletteInitData);
    }

    private static RouletteData createRouletteMultiplayerInitData(BigDecimal minimumBuyIn, Range<BigDecimal> betRange, Long roundDelay) {
        return new RouletteData(minimumBuyIn, betRange, RouletteTableFactory.DEFAULT_BETPHASE_TIME, 3, RouletteTableFactory.TABLE_NUMBERS, RouletteTableFactory.DEFAULT_HISTORY_SIZE, RouletteTableFactory.DEFAULT_SPIN_TIME_MILLIS, roundDelay);
    }

    private static RouletteData createRouletteSinglePlayerInitData(BigDecimal minimumBuyIn, Range<BigDecimal> betRange) {
        return new RouletteData(minimumBuyIn, betRange, null, 1, RouletteTableFactory.TABLE_NUMBERS, RouletteTableFactory.DEFAULT_HISTORY_SIZE, RouletteTableFactory.DEFAULT_SPIN_TIME_MILLIS, null);
    }

    private static TableData createTableInitData(Integer maxPlayer, Integer seatCount, TableType tableType) {
        TableThresholds thresholds = new TableThresholds(RouletteTableFactory.MIN_PLAYER_DEFAULT, maxPlayer, seatCount);
        return new TableData(RoulettePhasePathFactory.buildRoulettePhases(), TableStatus.WAITING_PLAYERS, thresholds, UUID.randomUUID(), Language.ENGLISH, tableType, Game.ROULETTE);
    }
}
