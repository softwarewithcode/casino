package com.casino.poker.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.casino.poker.game.HoldemPhase;

public class CheckTests extends DefaultTableTests {

    @Test
    public void preflopCheckFromLastPlayerToSpeakLeadsToFlopPhase() {
        defaultTableCallCheckToFlop();
        assertEquals(HoldemPhase.FLOP, table.getGamePhase());
    }

    @Test
    public void preflopCheckCausesDealerToDealFlop() {
        defaultTableCallCheckToFlop();
        assertEquals(3, table.getRound().getTableCards().size());
    }

    @Test
    public void preflopCheckChangesActivePlayerToNextPlayerFromButton() {
        defaultTableCallCheckToFlop();
        assertEquals(table.getRound().getBigBlindPlayer(), table.getActivePlayer());
    }

    @Test
    public void preflopCheckFromLastPlayerToSpeakCreatesMainPot() {
        defaultTableCallCheckToFlop();
        assertEquals(new BigDecimal("20.00"), table.getDealer().getPotHandler().getPots().get(0).getAmount());
    }

    @Test
    public void preflopCheckFromLastPlayerCreatesOnlyMainPot() {
        defaultTableCallCheckToFlop();
        assertEquals(1, table.getDealer().getPotHandler().getPots().size());
    }

    @Test
    public void flopCheckChangesActivePlayerToButtonPlayer() {
        defaultTableCallCheckToFlop();
        table.check(table.getRound().getPositions().bb().getId());
        assertEquals(table.getActivePlayer(), table.getRound().getPositions().buttonPlayer());
    }

    @Test
    public void flopCheckChangesActivePlayerToButtonToSmallBlindPlayer() {
        defaultTableCallCheckToFlop();
        table.check(table.getRound().getPositions().bb().getId());
        assertEquals(table.getActivePlayer(), table.getRound().getPositions().sb());
    }

    @Test
    public void potSizeDoesNotIncreaseWhenCheckingThroughFlop() {
        defaultTableCheckToTurn();
        assertEquals(new BigDecimal("20.00"), table.getDealer().getPotHandler().getPots().get(0).getAmount());
    }

    @Test
    public void gamePhaseChangesToTurnWhenCheckingThroughFlop() {
        defaultTableCheckToTurn();
        assertEquals(HoldemPhase.TURN, table.getGamePhase());
    }

    @Test
    public void gamePhaseChangesToRiverWhenCheckingThroughTurn() {
        defaultTableCheckToRiver();
        assertEquals(HoldemPhase.RIVER, table.getGamePhase());
    }
}
