package com.casino.poker.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

public class PotCalculationTest extends DefaultTableTests {
    @Test
    public void checksOnRiverLeadsToSplitPot() {
        defaultTableCheckToRiver();
        setupDefaultHeadsUpSplitPot();
        table.check(table.getRound().getPositions().bb().getId());
        table.check(table.getRound().getPositions().sb().getId());
        assertEquals(new BigDecimal("999.50"), table.getPlayer(user.userId()).getCurrentBalance());
        assertEquals(new BigDecimal("999.50"), table.getPlayer(user2.userId()).getCurrentBalance());
    }

    @Test
    public void bigBlindPlayerWinsThePot() {
        defaultTableCheckToRiver();
        setupCardsForWinnerAndLoser(getDefaultTableBigBlindPlayer(), getDefaultTableSmallBlindPlayer());
        table.check(table.getRound().getPositions().bb().getId());
        table.check(table.getRound().getPositions().sb().getId());
        assertEquals(new BigDecimal("1009.00"), getDefaultTableBigBlindPlayer().getCurrentBalance());
        assertEquals(new BigDecimal("990.00"), getDefaultTableSmallBlindPlayer().getCurrentBalance());
    }

    @Test
    public void smallBlindPlayerWinsThePot() {
        defaultTableCheckToRiver();
        setupCardsForWinnerAndLoser(getDefaultTableSmallBlindPlayer(), getDefaultTableBigBlindPlayer());
        table.check(table.getRound().getPositions().bb().getId());
        table.check(table.getRound().getPositions().sb().getId());
        assertEquals(new BigDecimal("1009.00"), getDefaultTableSmallBlindPlayer().getCurrentBalance());
        assertEquals(new BigDecimal("990.00"), getDefaultTableBigBlindPlayer().getCurrentBalance());
    }
}
