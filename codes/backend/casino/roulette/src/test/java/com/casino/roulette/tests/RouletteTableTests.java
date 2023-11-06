package com.casino.roulette.tests;

import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.common.exception.IllegalPlayerCountException;
import com.casino.common.ranges.Range;
import com.casino.roulette.game.RouletteData;
import com.casino.roulette.table.RouletteTable_;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class RouletteTableTests extends RouletteBaseTests {

    @Test
    public void joinTableIncreasesPlayerCount() {
        assertEquals(0, multiPlayerTable.getPlayerCount());
        multiPlayerTable.join(usr);
        assertEquals(1, multiPlayerTable.getPlayerCount());
    }

    @Test
    public void joinThrowsExceptionWhenTableHasNoFreePositions() { // not directly roulette test but SeatedTableTest
        multiPlayerTable = RouletteTableFactoryForTests.createMultiplayerTable(1);
        assertTrue(multiPlayerTable.join(usr));
        assertThrows(IllegalPlayerCountException.class, () -> multiPlayerTable.join(usr2));
    }

    @Test
    public void playFunctionalityThrowsExceptionInMultiPlayerTable() {
        multiPlayerTable = RouletteTableFactoryForTests.createMultiplayerTable(1);
        assertTrue(multiPlayerTable.join(usr));
        assertThrows(IllegalPlayerActionException.class, () -> multiPlayerTable.play(usr.getId(), multiPlayerTable.getId()));
    }

    @Test
    public void minBetCannotBeBiggerThanMinBuyIn() {
        Range<BigDecimal> betRange = new Range<>(TWENTY, THOUSAND);
        assertThrows(IllegalArgumentException.class, () -> new RouletteData(FIVE, betRange, null, null, null, null, null, null));
    }

    @Test
    public void negativeMinBetNotAllowed() {
        Range<BigDecimal> betRange = new Range<>(new BigDecimal("-1"), FIVE);
        assertThrows(IllegalArgumentException.class, () -> new RouletteData(THOUSAND, betRange, null, null, null, null, null, null));
    }

    @Test
    public void zeroMinBetNotAllowed() {
        Range<BigDecimal> betRange = new Range<>(new BigDecimal("0"), FIVE);
        assertThrows(IllegalArgumentException.class, () -> new RouletteData(THOUSAND, betRange, null, null, null, null, null, null));
    }

    @Test
    public void joiningWithInsufficientFundsThrowsError() {
        singlePlayerTable = (RouletteTable_) RouletteTableFactoryForTests.createHighRollerSinglePlayerTable();
        assertThrows(IllegalArgumentException.class, () -> singlePlayerTable.join(usr));
    }

    @Test
    public void minBetEqualsMinBuyIn() {
        singlePlayerTable = (RouletteTable_) RouletteTableFactoryForTests.createHighRollerSinglePlayerTable();
        assertThrows(IllegalArgumentException.class, () -> singlePlayerTable.join(usr));
    }
}
