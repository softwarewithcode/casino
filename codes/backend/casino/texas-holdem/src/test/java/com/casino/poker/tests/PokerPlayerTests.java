package com.casino.poker.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.casino.common.player.PlayerStatus;

public class PokerPlayerTests extends DefaultTableTests {
    @Test
    public void playerStatusIsNewBeforeNewGameStarts() {
        table.join(user, "2", false);
        assertTrue(table.getPlayer(2).getStatus() == PlayerStatus.NEW);
    }

    @Test
    public void playerGetsActiveStatusOnGameStart() {
        defaultJoinJoin();
        assertTrue(table.getPlayer(2).getStatus() == PlayerStatus.ACTIVE);
        assertTrue(table.getPlayer(3).getStatus() == PlayerStatus.ACTIVE);
    }

    @Test
    public void bbPlayerGetsTwoHoleCards() {
        defaultJoinJoin();
        assertEquals(2, table.getRound().getBigBlindPlayer().getHoleCards().size());
    }

    @Test
    public void sbPlayerGetsTwoHoleCards() {
        defaultJoinJoin();
        assertEquals(2, table.getRound().getSmallBlindPlayer().getHoleCards().size());
    }

    @Test
    public void preflopDealtHoleCardsAreRemovedFromDeck() {
        defaultJoinJoin();
        assertEquals(48, table.getDealer().getDeck().getCards().size());
    }

    @Test
    public void firstActivePlayerOnTheFlopIsNextFromButton() {
        defaultTableCallCheckToFlop();
        assertEquals(table.getActivePlayer(), table.getRound().getBigBlindPlayer());
    }

    @Test
    public void lastSpeakingPlayerOnTheFlopIsButtonPlayer() {
        defaultTableCallCheckToFlop();
        assertEquals(table.getRound().getLastSpeakingPlayer(), table.getRound().getPositions().buttonPlayer());
    }

    @Test
    public void firstActivePlayerOnTheTurnIsNextFromButton() {
        defaultTableCheckToTurn();
        assertEquals(table.getActivePlayer(), table.getRound().getBigBlindPlayer());
    }

    @Test
    public void lastSpeakingPlayerIsButtonPlayerWhenPhaseChanges() {
        defaultTableCheckToTurn();
        assertEquals(table.getRound().getLastSpeakingPlayer(), table.getRound().getSmallBlindPlayer());
    }

    @Test
    public void potSizeRemainSameWhenChecksThroughTurn() {
        defaultTableCheckToTurn();
        table.check(table.getRound().getPositions().bb().getId());
        table.check(table.getRound().getPositions().sb().getId());
        assertEquals(new BigDecimal("20.00"), table.getDealer().getPotHandler().getPots().get(0).getAmount());
    }
}
