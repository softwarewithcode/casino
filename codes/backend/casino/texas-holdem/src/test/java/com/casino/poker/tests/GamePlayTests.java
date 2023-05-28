package com.casino.poker.tests;

import com.casino.common.player.PlayerStatus;
import com.casino.poker.game.HoldemPhase;
import com.casino.poker.player.PokerPlayer;
import com.casino.poker.round.positions.PokerPositionsBuilder;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

public class GamePlayTests extends DefaultTableTests {
    @Test
    public void lastPlayerWinsThePotWhenEverybodyElseFoldsAndInitialButtonIsInSeat2() {
        //First round starts with the first 2 players
        table.join(bridge, "2", false); //1000
        table.join(bridge2, "3", false); //1000
        waitRoundToStart();
        table.allIn(table.getRound().getSmallBlindPlayer().getId());
        table.fold(table.getRound().getBigBlindPlayer().getId());
        table.join(bridge3, "4", false); //800
        table.join(bridge4, "5", false); //700
        table.join(bridge5, "1", false); //600
        table.join(bridge6, "0", false); //900
        sleep(DEFAULT_ROUND_DELAY_MILLIS, ChronoUnit.MILLIS);
        //Second round starts with 6 players.
        assignIrrevelantCardsForPlayers(table);
        PokerPlayer sb = table.getRound().getSmallBlindPlayer();
        PokerPlayer bb = table.getRound().getBigBlindPlayer();
        PokerPlayer utg = table.getNextSeat(bb.getSeatNumber(), true).getPlayer();
        PokerPlayer utgPlus1 = table.getNextSeat(utg.getSeatNumber(), true).getPlayer();
        PokerPlayer cutoff = table.getNextSeat(utgPlus1.getSeatNumber(), true).getPlayer();
        PokerPlayer button = table.getRound().getPositions().buttonPlayer();
        BigDecimal sbStartingStack = sb.getCurrentBalance().add(sb.getTableChipCount());
        BigDecimal bbStartingStack = bb.getCurrentBalance().add(bb.getTableChipCount());
        BigDecimal utgStartingStack = utg.getCurrentBalance().add(utg.getTableChipCount());
        BigDecimal utgPlus1StartingStack = utgPlus1.getCurrentBalance().add(utgPlus1.getTableChipCount());
        BigDecimal cutoffStartingStack = cutoff.getCurrentBalance().add(cutoff.getTableChipCount());
        BigDecimal buttonStartingStack = button.getCurrentBalance().add(button.getTableChipCount());
        assertSame(table.getGamePhase(), HoldemPhase.PRE_FLOP);
        table.raiseTo(table.getActivePlayer().getId(), new BigDecimal("41.27")); //UTG
        table.call(table.getActivePlayer().getId()); //UTGPlus1
        table.call(table.getActivePlayer().getId()); //CUTOFF
        table.call(table.getActivePlayer().getId());//BUTTON
        table.call(table.getActivePlayer().getId());//SB
        table.fold(table.getActivePlayer().getId());//BB
        assertSame(table.getGamePhase(), HoldemPhase.FLOP);
        assertEquals(new BigDecimal("216.35"), table.getDealer().getPotHandler().getActivePotAmount());
        assertEquals(1, table.getDealer().getPotHandler().getPots().size());
        table.raiseTo(table.getActivePlayer().getId(), new BigDecimal("10.01"));//SB
        table.call(table.getActivePlayer().getId());//UTG
        table.call(table.getActivePlayer().getId());//UTGPlus1
        table.call(table.getActivePlayer().getId());//CUTOFF
        table.call(table.getActivePlayer().getId());//BUTTON
        assertSame(table.getGamePhase(), HoldemPhase.TURN);
        assertEquals(new BigDecimal("266.40"), table.getDealer().getPotHandler().getActivePotAmount());
        assertEquals(1, table.getDealer().getPotHandler().getPots().size());
        table.raiseTo(table.getActivePlayer().getId(), new BigDecimal("44.99"));
        table.raiseTo(table.getActivePlayer().getId(), new BigDecimal("89.98"));
        table.call(table.getActivePlayer().getId());
        table.call(table.getActivePlayer().getId());
        table.call(table.getActivePlayer().getId());
        table.call(table.getActivePlayer().getId());
        assertSame(table.getGamePhase(), HoldemPhase.RIVER);
        assertEquals(new BigDecimal("716.30"), table.getDealer().getPotHandler().getActivePotAmount());
        assertEquals(1, table.getDealer().getPotHandler().getPots().size());
        assertEquals(5, table.getDealer().getPotHandler().getPots().get(0).getPlayers().size());
        assertEquals(table.getRound().getPositions().buttonPlayer(), table.getRound().getLastSpeakingPlayer());
        PokerPlayer lastActorOnRiver = table.getRound().getLastSpeakingPlayer();
        BigDecimal lastActorBalanceBeforeRiverActions = lastActorOnRiver.getCurrentBalance();
        table.fold(table.getActivePlayer().getId());//SB
        assertEquals(4, table.getDealer().getPotHandler().getPots().get(0).getPlayers().size());
        table.fold(table.getActivePlayer().getId());//UTG
        assertEquals(3, table.getDealer().getPotHandler().getPots().get(0).getPlayers().size());
        table.fold(table.getActivePlayer().getId());//UTGPlus1
        assertEquals(2, table.getDealer().getPotHandler().getPots().get(0).getPlayers().size());
        table.fold(table.getActivePlayer().getId());//CUTOFF
        assertSame(table.getGamePhase(), HoldemPhase.ROUND_COMPLETED);
        BigDecimal baseLostInRound = new BigDecimal("141.26");
        BigDecimal bbLostInRound = new BigDecimal("10.00");
        BigDecimal expectedWinnerEndBalance = lastActorBalanceBeforeRiverActions.add(table.getDealer().getPotHandler().getActivePotAmount());
        BigDecimal expectedAddition = buttonStartingStack
                .add(baseLostInRound)
                .add(baseLostInRound)
                .add(baseLostInRound)
                .add(baseLostInRound)
                .add(bbLostInRound).subtract(table.getDealer().getPotHandler().getPots().get(0).getRake());
        assertEquals(expectedAddition, button.getCurrentBalance());
        assertEquals(new BigDecimal("1565.04"), expectedWinnerEndBalance);
        assertEquals(new BigDecimal("1565.04"), button.getCurrentBalance());
        assertEquals(sbStartingStack.subtract(baseLostInRound), sb.getCurrentBalance());
        assertEquals(bbStartingStack.subtract(bbLostInRound), bb.getCurrentBalance());
        assertEquals(utgStartingStack.subtract(baseLostInRound), utg.getCurrentBalance());
        assertEquals(utgPlus1StartingStack.subtract(baseLostInRound), utgPlus1.getCurrentBalance());
        assertEquals(cutoffStartingStack.subtract(baseLostInRound), cutoff.getCurrentBalance());
        assertEquals(new BigDecimal("20.00"), table.getDealer().getPotHandler().getPots().get(0).getRake());
        assertEquals(new BigDecimal("696.30"), table.getDealer().getPotHandler().getPots().get(0).getAmount());
    }

    @Test
    public void newPlayersCannotJoinNextRoundBecauseButtonPlayerHasSmallBlind() {
        System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "3");
        defaultJoinJoin();
        assertEquals(table.getPlayer(3), table.getRound().getPositions().buttonPlayer());
        table.allIn(table.getRound().getSmallBlindPlayer().getId());
        table.fold(table.getRound().getBigBlindPlayer().getId());
        table.join(bridge3, "4", false); //800
        table.join(bridge4, "5", false); //700
        table.join(bridge5, "1", false); //600
        table.join(bridge6, "0", false); //900
        sleep(DEFAULT_ROUND_DELAY_MILLIS, ChronoUnit.MILLIS);
        assertEquals(table.getPlayer(2), table.getRound().getPositions().buttonPlayer());
        assertEquals(table.getPlayer(2), table.getRound().getPositions().sb());
        assertEquals(table.getPlayer(3), table.getRound().getPositions().bb());
        assertSame(table.getGamePhase(), HoldemPhase.PRE_FLOP);
        assertEquals(2, table.getRound().getPlayers().size());
        assertEquals(6, table.getReservedSeatCount());
        assertEquals(2, table.getActivePlayerCount());
        assertEquals(4, table.getNewPlayerCount());
        table.raiseTo(table.getActivePlayer().getId(), new BigDecimal("41.27"));
        table.call(table.getActivePlayer().getId());
        assertSame(table.getGamePhase(), HoldemPhase.FLOP);
    }

    @Test
    public void newPlayersAreIncludedInTheGameWhenBlindsSituationAllowsJoining() {
        System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "3");
        defaultJoinJoin();
        assertEquals(table.getPlayer(2), table.getRound().getPositions().bb());
        assertEquals(table.getPlayer(3), table.getRound().getPositions().sb());
        assertEquals(table.getPlayer(3), table.getRound().getPositions().buttonPlayer());
        table.allIn(table.getRound().getSmallBlindPlayer().getId());
        table.join(bridge3, "4", false); //800
        table.join(bridge4, "5", false); //700
        table.join(bridge5, "1", false); //600
        table.join(bridge6, "0", false); //900
        table.fold(table.getRound().getBigBlindPlayer().getId());
        sleep(DEFAULT_ROUND_DELAY_MILLIS, ChronoUnit.MILLIS);
        assertSame(table.getGamePhase(), HoldemPhase.PRE_FLOP);
        assertEquals(2, table.getRounds().size());
        assertEquals(table.getPlayer(2), table.getRound().getPositions().buttonPlayer());
        assertEquals(table.getPlayer(2), table.getRound().getPositions().sb());
        assertEquals(table.getPlayer(3), table.getRound().getPositions().bb());
        assertEquals(2, table.getRound().getPlayers().size());
        assertEquals(6, table.getReservedSeatCount());
        assertEquals(2, table.getActivePlayerCount());
        assertEquals(4, table.getNewPlayerCount());
        table.fold(table.getActivePlayer().getId());
        sleep(DEFAULT_ROUND_DELAY_MILLIS, ChronoUnit.MILLIS);
        assertSame(table.getGamePhase(), HoldemPhase.PRE_FLOP);
        assertEquals(6, table.getReservedSeatCount());
        assertEquals(6, table.getActivePlayerCount());
        assertEquals(0, table.getNewPlayerCount());
        assertEquals(table.getPlayer(2), table.getRound().getPositions().buttonPlayer());
        assertEquals(table.getPlayer(3), table.getRound().getPositions().sb());
        assertEquals(table.getPlayer(4), table.getRound().getPositions().bb());
    }

    @Test
    public void buttonDoesNotMoveWhenNewPlayerEntersInBetweenBlindsAndNewPlayerGetsBigBlind() {
        System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "1");
        table.join(bridge, "5", false); //1000
        table.join(bridge2, "1", false); //1000
        waitRoundToStart();
        assertEquals(table.getPlayer(1), table.getRound().getPositions().buttonPlayer());
        assertEquals(table.getPlayer(1), table.getRound().getPositions().sb());
        assertEquals(table.getPlayer(5), table.getRound().getPositions().bb());
        table.allIn(table.getRound().getSmallBlindPlayer().getId());
        table.join(bridge3, "0", false); //800
        table.join(bridge4, "2", false); //700
        assertEquals(2, table.getNewPlayerCount());
        table.join(bridge5, "3", false); //600
        table.join(bridge6, "4", false); //900
        assertEquals(4, table.getNewPlayerCount());
        table.fold(table.getRound().getBigBlindPlayer().getId());
        sleep(DEFAULT_ROUND_DELAY_MILLIS, ChronoUnit.MILLIS);
        assertEquals(2, table.getRounds().size());
        assertSame(table.getGamePhase(), HoldemPhase.PRE_FLOP);
        assertEquals(table.getPlayer(1), table.getRound().getPositions().buttonPlayer());
        assertEquals(table.getPlayer(5), table.getRound().getPositions().sb());
        assertEquals(table.getPlayer(0), table.getRound().getPositions().bb());
        assertEquals(6, table.getReservedSeatCount());
        assertEquals(3, table.getActivePlayerCount());
        assertEquals(3, table.getNewPlayerCount());
        assertSame(table.getGamePhase(), HoldemPhase.PRE_FLOP);
        assertEquals(1, table.getPlayer(table.getActivePlayer().getId()).getSeatNumber());
    }

    @Test
    public void joiningInBetweenButtonAndBigBlindDuringFirstRoundMakesPlayerWait2RoundsUntilGetsDealtIn() {
        System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "0");
        table.join(bridge, "0", false); //1000
        table.join(bridge2, "4", false); //1000
        waitRoundToStart();
        assertEquals(table.getPlayer(0), table.getRound().getPositions().buttonPlayer());
        assertEquals(table.getPlayer(0), table.getRound().getPositions().sb());
        assertEquals(table.getPlayer(4), table.getRound().getPositions().bb());
        table.allIn(table.getRound().getSmallBlindPlayer().getId());
        table.join(bridge3, "3", false); //800
        table.fold(table.getRound().getBigBlindPlayer().getId());
        sleep(DEFAULT_ROUND_DELAY_MILLIS, ChronoUnit.MILLIS);
        //Round 2
        assertEquals(table.getPlayer(4), table.getRound().getPositions().buttonPlayer());
        assertEquals(table.getPlayer(4), table.getRound().getPositions().sb());
        assertEquals(table.getPlayer(0), table.getRound().getPositions().bb());
        assertEquals(2, table.getActivePlayerCount());
        assertEquals(1, table.getNewPlayerCount());
        assertEquals(3, table.getPlayerCount());
        assertEquals(2, table.getRounds().size());
        assertEquals(table.getPlayer(4), table.getActivePlayer());
        table.allIn(table.getActivePlayer().getId());
        table.fold(table.getActivePlayer().getId());
        //Round 3
        sleep(DEFAULT_ROUND_DELAY_MILLIS, ChronoUnit.MILLIS);
        assertEquals(table.getPlayer(4), table.getRound().getPositions().buttonPlayer());
        assertEquals(table.getPlayer(0), table.getRound().getPositions().sb());
        assertEquals(table.getPlayer(3), table.getRound().getPositions().bb());
        assertEquals(3, table.getActivePlayerCount());
        assertEquals(0, table.getNewPlayerCount());
        assertEquals(3, table.getPlayerCount());
        assertEquals(3, table.getRounds().size());
        assertEquals(table.getPlayer(4), table.getActivePlayer());
    }

    @Test
    public void takingSeatDuringRound2GivesBigBlindPositionToLatestJoiner() {
        System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "0");
        table.join(bridge, "0", false); //1000
        table.join(bridge2, "4", false); //1000
        waitRoundToStart();
        table.allIn(table.getRound().getSmallBlindPlayer().getId());
        table.join(bridge3, "3", false); //800
        table.fold(table.getRound().getBigBlindPlayer().getId());
        sleep(DEFAULT_ROUND_DELAY_MILLIS, ChronoUnit.MILLIS);
        //Round 2
        table.join(bridge4, "1", false);
        assertEquals(2, table.getActivePlayerCount());
        assertEquals(2, table.getNewPlayerCount());
        assertEquals(4, table.getPlayerCount());
        table.allIn(table.getActivePlayer().getId());
        table.fold(table.getActivePlayer().getId());
        //Round 3
        sleep(DEFAULT_ROUND_DELAY_MILLIS, ChronoUnit.MILLIS);
        assertEquals(4, table.getActivePlayerCount());
        assertEquals(0, table.getNewPlayerCount()); // Seat 3 doesn't wait for bigBlind
        assertEquals(4, table.getPlayerCount());
        assertEquals(table.getPlayer(4), table.getRound().getPositions().buttonPlayer());
        assertEquals(table.getPlayer(0), table.getRound().getPositions().sb());
        assertEquals(table.getPlayer(1), table.getRound().getPositions().bb());
    }

    @Test
    public void waitingForBigBlindMakesPlayerWait3HandsBeforeJoiningTheGame() {
        System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "0");
        table.join(bridge, "0", false);
        table.join(bridge2, "4", false);
        waitRoundToStart();
        assertEquals(table.getPlayer(0), table.getRound().getPositions().buttonPlayer());
        assertEquals(table.getPlayer(0), table.getRound().getPositions().sb());
        assertEquals(table.getPlayer(4), table.getRound().getPositions().bb());
        table.allIn(table.getRound().getSmallBlindPlayer().getId());
        table.join(bridge3, "3", true);
        table.fold(table.getRound().getBigBlindPlayer().getId());
        sleep(DEFAULT_ROUND_DELAY_MILLIS, ChronoUnit.MILLIS);
        //Round 2
        assertSame(table.getGamePhase(), HoldemPhase.PRE_FLOP);
        assertEquals(table.getPlayer(4), table.getRound().getPositions().buttonPlayer());
        assertEquals(table.getPlayer(4), table.getRound().getPositions().sb());
        assertEquals(table.getPlayer(0), table.getRound().getPositions().bb());
        table.join(bridge4, "1", false);
        assertTrue(table.getPlayer(3).isWaitingBigBlind());
        assertEquals(2, table.getActivePlayerCount());
        assertEquals(2, table.getNewPlayerCount());
        assertEquals(4, table.getPlayerCount());
        table.allIn(table.getActivePlayer().getId());
        table.fold(table.getActivePlayer().getId());
        sleep(DEFAULT_ROUND_DELAY_MILLIS, ChronoUnit.MILLIS);
        //Round 3
        assertSame(table.getGamePhase(), HoldemPhase.PRE_FLOP);
        assertEquals(3, table.getRounds().size());
        assertEquals(3, table.getActivePlayerCount());
        assertEquals(1, table.getNewPlayerCount()); // Seat 3 waits bigBlind
        assertEquals(4, table.getPlayerCount());
        assertEquals(table.getPlayer(4), table.getRound().getPositions().buttonPlayer());
        assertEquals(table.getPlayer(0), table.getRound().getPositions().sb());
        assertEquals(table.getPlayer(1), table.getRound().getPositions().bb());
        table.call(table.getActivePlayer().getId());
        table.call(table.getActivePlayer().getId());
        table.check(table.getActivePlayer().getId());
        assertSame(table.getGamePhase(), HoldemPhase.FLOP);
        table.allIn(table.getActivePlayer().getId());
        table.fold(table.getActivePlayer().getId());
        table.fold(table.getActivePlayer().getId());
        sleep(DEFAULT_ROUND_DELAY_MILLIS, ChronoUnit.MILLIS);
        //Round 4
        assertSame(table.getGamePhase(), HoldemPhase.PRE_FLOP);
        assertEquals(4, table.getRounds().size());
        assertEquals(4, table.getActivePlayerCount());
        assertEquals(0, table.getNewPlayerCount());
        assertEquals(4, table.getPlayerCount());
        assertEquals(table.getPlayer(0), table.getRound().getPositions().buttonPlayer());
        assertEquals(table.getPlayer(1), table.getRound().getPositions().sb());
        assertEquals(table.getPlayer(3), table.getRound().getPositions().bb());
        assertFalse(table.getPlayer(3).isWaitingBigBlind());
    }

    @Test
    public void playersWaitingForBigBlindAreAddedGradually() {
        System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "1");
        table.join(bridge, "0", false);
        table.join(bridge2, "1", false);
        waitRoundToStart();
        table.join(bridge3, "2", true);
        table.join(bridge4, "3", true);
        waitRoundToStart();
        table.allIn(table.getRound().getSmallBlindPlayer().getId());
        table.fold(table.getRound().getBigBlindPlayer().getId());
        sleep(DEFAULT_ROUND_DELAY_MILLIS, ChronoUnit.MILLIS);
        //Round 2 with 2 players
        assertEquals(table.getPlayer(0), table.getRound().getPositions().buttonPlayer());
        assertEquals(table.getPlayer(0), table.getRound().getPositions().sb());
        assertEquals(table.getPlayer(1), table.getRound().getPositions().bb());
        assertEquals(2, table.getActivePlayerCount());
        assertEquals(2, table.getNewPlayerCount());
        assertTrue(table.getPlayer(2).isWaitingBigBlind());
        assertTrue(table.getPlayer(3).isWaitingBigBlind());
        table.allIn(table.getRound().getSmallBlindPlayer().getId());
        table.fold(table.getRound().getBigBlindPlayer().getId());
        sleep(DEFAULT_ROUND_DELAY_MILLIS, ChronoUnit.MILLIS);
        //Round 3 with 3 players
        assertEquals(table.getPlayer(0), table.getRound().getPositions().buttonPlayer());
        assertEquals(table.getPlayer(1), table.getRound().getPositions().sb());
        assertEquals(table.getPlayer(2), table.getRound().getPositions().bb());
        assertEquals(3, table.getActivePlayerCount());
        assertEquals(1, table.getNewPlayerCount());
        assertFalse(table.getPlayer(2).isWaitingBigBlind());
        assertTrue(table.getPlayer(3).isWaitingBigBlind());
        table.allIn(table.getActivePlayer().getId());
        table.fold(table.getActivePlayer().getId());
        table.fold(table.getActivePlayer().getId());
        sleep(DEFAULT_ROUND_DELAY_MILLIS, ChronoUnit.MILLIS);
        //Round 4 with 4 players
        assertEquals(table.getPlayer(1), table.getRound().getPositions().buttonPlayer());
        assertEquals(table.getPlayer(2), table.getRound().getPositions().sb());
        assertEquals(table.getPlayer(3), table.getRound().getPositions().bb());
        assertEquals(4, table.getActivePlayerCount());
        assertEquals(0, table.getNewPlayerCount());
        assertFalse(table.getPlayer(2).isWaitingBigBlind());
        assertFalse(table.getPlayer(3).isWaitingBigBlind());
    }

    @Test
    public void sitOutPlayerHasStatusOf_SitOutAsNew_WhenNewPlayerJoinsAndNewGameIsStartedWithoutSitouPlayer() {
        defaultJoinJoin();
        sleep(TestHoldemTableFactory.DEFAULT_PLAYER_TIME, ChronoUnit.SECONDS);
        assertEquals(table.getPlayer(2).getStatus(), PlayerStatus.SIT_OUT);
        table.join(bridge6, "5", false);
        waitRoundToStart();
        assertEquals(table.getPlayer(2).getStatus(), PlayerStatus.SIT_OUT_AS_NEW);
        assertEquals(2, table.getRounds().size());
        assertEquals(2, table.getActivePlayerCount());
        assertEquals(PlayerStatus.ACTIVE, table.getPlayer(3).getStatus());
        assertEquals(PlayerStatus.ACTIVE, table.getPlayer(5).getStatus());
    }
}
