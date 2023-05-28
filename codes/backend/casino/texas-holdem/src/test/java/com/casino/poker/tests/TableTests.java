package com.casino.poker.tests;

import com.casino.common.table.TableStatus;
import com.casino.poker.game.HoldemPhase;
import com.casino.poker.player.PokerPlayer;
import com.casino.poker.round.PokerRound;
import com.casino.poker.round.positions.PokerPositionsBuilder;
import com.casino.poker.table.PokerTable;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

public class TableTests extends DefaultTableTests {

    @Test
    public void playerGetsSeat() {
        assertTrue(table.join(bridge, "2", false));
        assertNotNull(table.getPlayer(2));
    }

    @Test
    public void playerDoesNotGetReservedSeat() {
        table.join(bridge, "2", false);
        assertFalse(table.join(bridge2, "2", false));
    }

    @Test
    public void playerCannotGetTwoSeats() {
        table.join(bridge, "2", false);
        assertFalse(table.join(bridge, "3", false));
    }

    @Test
    public void minimumBuyInMustBeCoveredToJoinTable() {
        assertThrows(IllegalArgumentException.class, () -> {
            table.join(bridgeWithoutMoney, "2", false);
        });
    }

    @Test
    public void tableInitialStatusIsWaitingForPlayers() {
        assertSame(table.getStatus(), TableStatus.WAITING_PLAYERS);
    }

    @Test
    public void tableStatusIsWaitingForPlayersAfterFirstJoiner() {
        table.join(bridge, "2", false);
        assertSame(table.getStatus(), TableStatus.WAITING_PLAYERS);
    }

    @Test
    public void tableIsStartedAfterSecondJoiner() {
        defaultJoinJoin();
        assertSame(table.getStatus(), TableStatus.RUNNING);
    }

    @Test
    public void deckContains45CardsOnTheFlop() {
        defaultTableCallCheckToFlop();
        assertEquals(45, table.getDealer().getDeck().getCards().size());
    }

    @Test
    public void tableHas3CardsOnTheFlop() {
        defaultTableCallCheckToFlop();
        assertEquals(3, table.getRound().getTableCards().size());
    }

    @Test
    public void completedRoundIsInHistoryState() {
        defaultTableCheckRoundThrough();
        sleep(getDefaultDealer().getGameData().getRoundDelay().intValue() + 1000, ChronoUnit.MILLIS);
        PokerTable<PokerPlayer> table = (PokerTable<PokerPlayer>) getDefaultDealer().getTable();
        assertEquals(1, table.getRounds().stream().filter(PokerRound::isCompleted).toList().size());
    }

    @Test
    public void joiningTableDuringPreFlopPhaseIsPossible() {
        defaultJoinJoin();
        table.join(bridge3, "4", false);
        assertEquals(3, table.getPlayers().size());
        assertEquals(2, table.getRound().getPlayers().size());
        assertEquals(HoldemPhase.PRE_FLOP, table.getGamePhase());
    }

    @Test
    public void joiningTableDuringFlopPhaseIsPossible() {
        defaultTableCallCheckToFlop();
        table.join(bridge3, "4", false);
        assertEquals(3, table.getPlayers().size());
        assertEquals(2, table.getRound().getPlayers().size());
        assertEquals(HoldemPhase.FLOP, table.getGamePhase());
    }

    @Test
    public void joiningTableDuringTurnPhaseIsPossible() {
        defaultTableCheckToTurn();
        table.join(bridge3, "4", false);
        assertEquals(3, table.getPlayers().size());
        assertEquals(2, table.getRound().getPlayers().size());
        assertEquals(HoldemPhase.TURN, table.getGamePhase());
    }

    @Test
    public void joiningTableDuringRiverPhaseIsPossible() {
        defaultTableCheckToRiver();
        table.join(bridge3, "4", false);
        assertEquals(3, table.getPlayers().size());
        assertEquals(2, table.getRound().getPlayers().size());
        assertEquals(HoldemPhase.RIVER, table.getGamePhase());
    }

    @Test
    public void joiningTableIsPossibleDuringRunningRound() {
        defaultJoinJoin();
        table.join(bridge3, null, false);
        table.join(bridge4, null, false);
        assertEquals(4, table.getPlayers().size());
        assertEquals(2, table.getRound().getPlayers().size());
        table.call(getDefaultTableSmallBlindPlayer().getId());
        table.check(getDefaultTableBigBlindPlayer().getId());
        table.join(bridge5, null, false);
        assertEquals(5, table.getPlayers().size());
        assertEquals(2, table.getRound().getPlayers().size());
        table.check(getDefaultTableBigBlindPlayer().getId());
        table.join(bridge6, null, false);
        table.check(getDefaultTableSmallBlindPlayer().getId());
        assertEquals(6, table.getPlayers().size());
        assertEquals(2, table.getRound().getPlayers().size());
        table.check(getDefaultTableBigBlindPlayer().getId());
        assertFalse(table.join(bridge2, null, false));
    }

    @Test
    public void playersWhoJoinedDuringRoundAreAddedToNextRoundWhenTheyAreNotWaitingForBigBlind() {
        defaultJoinJoin();
        table.join(bridge3, null, false);
        table.join(bridge4, null, false);
        table.call(getDefaultTableSmallBlindPlayer().getId());
        table.check(getDefaultTableBigBlindPlayer().getId());
        table.join(bridge5, null, false);
        table.check(getDefaultTableBigBlindPlayer().getId());
        table.join(bridge6, null, false);
        table.check(getDefaultTableSmallBlindPlayer().getId());
        table.check(getDefaultTableBigBlindPlayer().getId());
        table.check(getDefaultTableSmallBlindPlayer().getId());
        table.check(getDefaultTableBigBlindPlayer().getId());
        table.check(getDefaultTableSmallBlindPlayer().getId());
        sleep(DEFAULT_ROUND_DELAY_MILLIS, ChronoUnit.MILLIS);
        assertEquals(HoldemPhase.PRE_FLOP, table.getGamePhase());
        assertEquals(6, table.getPlayers().size());
        assertEquals(6, table.getRound().getPlayers().size());
    }

    @Test
    public void joinedPlayersWhoWaitForBigBlindAreNotAddedToRound() {
        defaultJoinJoin();
        table.join(bridge3, "4", true);
        table.join(bridge4, null, true);
        table.call(getDefaultTableSmallBlindPlayer().getId());
        table.check(getDefaultTableBigBlindPlayer().getId());
        table.join(bridge5, null, false);
        table.check(getDefaultTableBigBlindPlayer().getId());
        table.join(bridge6, null, false);
        table.check(getDefaultTableSmallBlindPlayer().getId());
        table.check(getDefaultTableBigBlindPlayer().getId());
        table.check(getDefaultTableSmallBlindPlayer().getId());
        table.check(getDefaultTableBigBlindPlayer().getId());
        table.check(getDefaultTableSmallBlindPlayer().getId());
        sleep(DEFAULT_ROUND_DELAY_MILLIS, ChronoUnit.MILLIS);
        assertEquals(HoldemPhase.PRE_FLOP, table.getGamePhase());
        assertEquals(6, table.getPlayers().size());
        assertEquals(5, table.getRound().getPlayers().size()); //TODO fails occasionally
    }

    @Test
    public void joinedPlayersWhoWaitForBigBlindAreNotAddedToRound_part2() {
        defaultJoinJoin();
        table.join(bridge3, "0", true);
        table.join(bridge4, "1", true);
        table.call(getDefaultTableSmallBlindPlayer().getId());
        table.check(getDefaultTableBigBlindPlayer().getId());
        table.join(bridge5, null, false);
        table.check(getDefaultTableBigBlindPlayer().getId());
        table.join(bridge6, null, false);
        table.check(getDefaultTableSmallBlindPlayer().getId());
        table.check(getDefaultTableBigBlindPlayer().getId());
        table.check(getDefaultTableSmallBlindPlayer().getId());
        table.check(getDefaultTableBigBlindPlayer().getId());
        table.check(getDefaultTableSmallBlindPlayer().getId());
        sleep(DEFAULT_ROUND_DELAY_MILLIS, ChronoUnit.MILLIS);
        assertEquals(HoldemPhase.PRE_FLOP, table.getGamePhase());
        assertEquals(6, table.getPlayers().size());
        assertEquals(4, table.getRound().getPlayers().size());
    }

    @Test
    public void allPlayersAreAddedToNewRound() {
        defaultTableCheckRoundThrough();
        table.join(bridge3, "4", false);
        table.join(bridge4, "5", false);
        table.join(bridge5, "1", false);
        table.join(bridge6, "0", false);
        sleep(DEFAULT_ROUND_DELAY_MILLIS, ChronoUnit.MILLIS);
        assertEquals(6, table.getRound().getPlayers().size());
    }


    @Test
    public void toggleWaitBigBlindSetsWaitingMode() {
        defaultJoinJoin();
        table.join(bridge4, "4", false);
        assertTrue(table.toggleWaitBigBlind(bridge4.userId()));
        assertTrue(table.getPlayer(bridge4.userId()).isWaitingBigBlind());
    }

    @Test
    public void toggleWaitBigBlindResetsWaitingMode() {
        defaultTableCheckToRiver();
        table.join(bridge4, "4", true);
        table.check(getDefaultTableBigBlindPlayer().getId());
        table.check(getDefaultTableSmallBlindPlayer().getId());
        assertTrue(table.getPlayer(bridge4.userId()).isWaitingBigBlind());
        assertFalse(table.toggleWaitBigBlind(bridge4.userId()));
        assertFalse(table.getPlayer(bridge4.userId()).isWaitingBigBlind());
        sleep(DEFAULT_ROUND_DELAY_MILLIS, ChronoUnit.MILLIS);
        assertFalse(table.getPlayer(bridge4.userId()).isWaitingBigBlind());
        assertEquals(3, table.getRound().getPlayers().size());
    }

    @Test
    public void waitForBigBlindIsIgnoredDuringFirstRoundOfTable() {
        System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "3");
        default6PlayersJoin6MaxTable();
        assertEquals(6, table.getRound().getPlayers().size());
    }

    @Test
    public void pokerPositionsAreFormedCorrectlyWith6PlayersFirstRound() {
        System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "0");
        default6PlayersJoin6MaxTable();
        assertEquals(table.getPlayer(0), table.getRound().getPositions().buttonPlayer());
        assertEquals(table.getPlayer(1), table.getRound().getSmallBlindPlayer());
        assertEquals(table.getPlayer(2), table.getRound().getBigBlindPlayer());
    }
}
