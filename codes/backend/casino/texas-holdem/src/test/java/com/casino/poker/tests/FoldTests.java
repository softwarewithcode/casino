package com.casino.poker.tests;


import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FoldTests extends DefaultTableTests {
    @Test
    public void bigBlindPlayerWinsPotWhenSmallBlindPlayerFoldsPreFlop() {
        defaultJoinJoin();
        table.fold(getDefaultTableSmallBlindPlayer().getId());
        assertEquals(new BigDecimal("1005.00"), table.getRound().getBigBlindPlayer().getCurrentBalance());
        assertEquals(new BigDecimal("995.00"), table.getRound().getSmallBlindPlayer().getCurrentBalance());
    }

    @Test
    public void sbPlayerWinsByRaisingPreFlopAndBigBlindPlayerFolds() {
        defaultJoinJoin();
        table.raiseTo(getDefaultTableSmallBlindPlayer().getId(), new BigDecimal("100.11"));
        table.fold(getDefaultTableBigBlindPlayer().getId());
        assertEquals(new BigDecimal("1010.00"), table.getRound().getSmallBlindPlayer().getCurrentBalance());
        assertEquals(new BigDecimal("990.00"), table.getRound().getBigBlindPlayer().getCurrentBalance());
    }

    @Test
    public void bigBlindPlayerWinsCalledAmountAfterRaisingAndSmallBlindPlayerFolds() {
        defaultJoinJoin();
        table.call(getDefaultTableSmallBlindPlayer().getId());
        table.raiseTo(getDefaultTableBigBlindPlayer().getId(), new BigDecimal("100.11"));
        table.fold(getDefaultTableSmallBlindPlayer().getId());
        assertEquals(new BigDecimal("1010.00"), table.getRound().getBigBlindPlayer().getCurrentBalance());
        assertEquals(new BigDecimal("990.00"), table.getRound().getSmallBlindPlayer().getCurrentBalance());
    }

    @Test
    public void bigBlindPlayerWinsWhenSmallBlindPlayerFolds() {
        defaultJoinJoin();
        table.call(getDefaultTableSmallBlindPlayer().getId());
        table.raiseTo(getDefaultTableBigBlindPlayer().getId(), new BigDecimal("100.11"));
        table.call(getDefaultTableSmallBlindPlayer().getId());
        assertEquals(new BigDecimal("200.22"), table.getDealer().getPotHandler().getPots().get(0).getAmount());
        table.raiseTo(getDefaultTableBigBlindPlayer().getId(), new BigDecimal("11.12"));
        assertEquals(new BigDecimal("200.22"), table.getDealer().getPotHandler().getPots().get(0).getAmount());
        assertEquals(new BigDecimal("11.12"), getDefaultTableBigBlindPlayer().getTableChipCount());
        table.fold(getDefaultTableSmallBlindPlayer().getId());
        assertEquals(new BigDecimal("00.00"), getDefaultTableBigBlindPlayer().getTableChipCount());
        assertEquals(new BigDecimal("1090.09"), getDefaultTableBigBlindPlayer().getCurrentBalance());
    }

}


