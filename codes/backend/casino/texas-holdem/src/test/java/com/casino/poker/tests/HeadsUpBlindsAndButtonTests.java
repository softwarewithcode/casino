package com.casino.poker.tests;

import com.casino.poker.actions.PokerActionType;
import com.casino.poker.functions.HoldemFunctions;
import com.casino.poker.game.HoldemPhase;
import com.casino.poker.player.HoldemPlayer;
import com.casino.poker.player.PokerPlayer;
import com.casino.poker.round.positions.PokerRoundPlayers;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class HeadsUpBlindsAndButtonTests extends DefaultTableTests {

    @Test
    public void gameStartsInPreFlopPhase() {
        defaultJoinJoin();
        assertEquals(HoldemPhase.PRE_FLOP, table.getGamePhase());
    }

    @Test
    public void smallBlindIsPutOnTable() {
        defaultJoinJoin();
        assertEquals(new BigDecimal("5.00"), table.getRound().getSmallBlindPlayer().getTableChipCount());
    }

    @Test
    public void bigBlindIsPutOnTable() {
        defaultJoinJoin();
        assertEquals(new BigDecimal("10.00"), table.getRound().getBigBlindPlayer().getTableChipCount());
    }

    @Test
    public void buttonPositionIsUndeterminedWhenTableIsNotYetStarted() {
        assertNull(table.getButton().getSeatNumber());
    }

    @Test
    public void onePlayerGetsButtonOnGameStart() {
        defaultJoinJoin();
        Integer seatNumber = table.getButton().getSeatNumber();
        assertTrue(seatNumber >= 0 && seatNumber < table.getSeats().size());
        assertNotNull(table.getPlayer(seatNumber));
    }

    @Test
    public void samePlayerCannotHaveBothBlinds() {
        PokerPlayer p1 = new HoldemPlayer(user3, table);
        PokerPlayer p2 = new HoldemPlayer(user4, table);
        assertThrows(IllegalArgumentException.class, () -> new PokerRoundPlayers(p1, p1, p2, 0, 2, 3, null));
    }

    @Test
    public void bothBlindsAreIncludedInTotalTableMoney() {
        defaultJoinJoin();
        assertEquals(new BigDecimal("15.00"), table.getDealer().countAllPlayersChipsOnTable());
    }

    @Test
    public void preFlopCallIsAnOptionForSmallBlindPlayer() {
        defaultJoinJoin();
        assertTrue(HoldemFunctions.hasAction.apply(table.getRound().getSmallBlindPlayer(), PokerActionType.CALL));
    }

    @Test
    public void preFlopFoldIsAnOptionForSmallBlindPlayer() {
        defaultJoinJoin();
        assertTrue(HoldemFunctions.hasAction.apply(table.getRound().getSmallBlindPlayer(), PokerActionType.FOLD));
    }

    @Test
    public void preFlopAllInIsAnOptionForSmallBlindPlayer() {
        defaultJoinJoin();
        assertTrue(HoldemFunctions.hasAction.apply(table.getRound().getSmallBlindPlayer(), PokerActionType.ALL_IN));
    }

    @Test
    public void sbPlayerHasFourOptionsPreflop() {
        defaultJoinJoin();
        assertEquals(4, table.getRound().getSmallBlindPlayer().getActions().size());
    }

    @Test
    public void bigBlindPlayerHasNoInitialOptionsPreFlop() {
        defaultJoinJoin();
        assertEquals(0, table.getRound().getBigBlindPlayer().getActions().size());
    }

    @Test
    public void sbPlayerIsFirstActivePlayerPreFlop() {
        defaultJoinJoin();
        assertEquals(table.getActivePlayer(), table.getRound().getSmallBlindPlayer());
    }

    @Test
    public void bbPlayerIsLastToSpeak() {
        defaultJoinJoin();
        assertEquals(table.getRound().getLastSpeakingPlayer(), table.getRound().getBigBlindPlayer());
    }

    @Test
    public void bbPlayerHasNoPreFlopActionsBeforeSbPlayerHasActed() {
        defaultJoinJoin();
        assertEquals(0, table.getRound().getBigBlindPlayer().getActions().size());
    }

    @Test
    public void roundIsCompletedAfterPayout() {
        defaultTableCheckRoundThrough();
        assertNotNull(table.getRound().getCompleted());
    }

    @Test
    public void buttonChangesFromSmallBlindPlayerToBigBlindPlayerBasedOnPreviousRound() {
        defaultTableCheckRoundThrough();
        //assertEquals(0, defaultTable.getRound().getBigBlindPlayer().getActions().size());
    }

}
