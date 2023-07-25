package com.casino.poker.tests;

import com.casino.common.player.PlayerStatus;
import com.casino.common.table.TableStatus;
import com.casino.poker.game.HoldemPhase;
import com.casino.poker.hand.PokerHandType;
import com.casino.poker.player.HoldemPlayer;
import com.casino.poker.player.PokerPlayer;
import com.casino.poker.round.positions.PokerPositionsBuilder;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class LeavingPlayersTest extends DefaultTableTests {

    @Test
    public void playerWinsPotWithWhenLeavingPlayerHandIsFolded() {
        System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "3");
        //PRE FLOP
        defaultJoinJoin();
        assertEquals(new BigDecimal("990.00"), table.getPlayer(2).getCurrentBalance());
        table.leave(bridge2.userId());
        assertEquals(new BigDecimal("1005.00"), table.getPlayer(2).getCurrentBalance());
        assertEquals(1, table.getActivePlayerCount());
        assertEquals(1, table.getReservedSeatCount());
        assertEquals(0, table.getSitOutPlayerCount());
    }

    @Test
    public void leavingPlayerWinsPotAfterAutoCheckingThroughRiverCard() {
        System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "3");
        //PRE FLOP
        defaultJoinJoin();
        table.call(bridge2.userId());
        table.leave(bridge2.userId());
        //Pre flop check from bridge2
        table.check(bridge.userId());
        //Flop check from bridge2
        table.check(bridge.userId());
        //Turn check from bridge2
        table.check(bridge.userId());
        setupCardsForWinnerAndLoser(table.getPlayer(3), table.getPlayer(2));
        //River check from bridge2
        table.check(bridge.userId());
        assertEquals(0, table.getSitOutPlayerCount());
        assertNotNull(table.getPlayer(2).getHand());
        assertEquals(new BigDecimal("990.00"),table.getPlayer(2).getCurrentBalance());
       //assertEquals(new BigDecimal("1009.00"), defaultTable.getPlayer(3).getCurrentBalance());
    }

    @Test
    public void bothHeadsUpPlayersLeaveAndHandIsCompletedByAutoplay() {
        System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "3");
        defaultJoinJoin();
        table.call(bridge2.userId());
        assertEquals(HoldemPhase.PRE_FLOP, table.getGamePhase());
        table.leave(bridge.userId());
        table.leave(bridge2.userId());
        waitRoundToStart();
        assertEquals(HoldemPhase.ROUND_COMPLETED, table.getGamePhase());
        assertEquals(TableStatus.WAITING_PLAYERS, table.getStatus());
        assertNull(table.getPlayer(2));
        assertNull(table.getPlayer(3));
    }

    @Test
    public void bigBlindLPlayerLeavesInStartOfRoundAndAndNextSmallBlindPlayerDoesNotExist() {
        System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "0");
        default6PlayersJoin6MaxTable();
        table.leave(table.getPlayer(2).getId()); //BB leaves
        table.fold(table.getPlayer(3).getId()); //UTG
        table.fold(table.getPlayer(4).getId()); //UTG+1
        table.fold(table.getPlayer(5).getId()); //cutoff
        table.fold(table.getPlayer(0).getId()); //button
        table.fold(table.getPlayer(1).getId()); //sb
        assertEquals(new BigDecimal("995.00"), table.getPlayer(1).getCurrentBalance());
        assertNull(table.getPlayer(2));
        waitRoundToStart();
        //Second round
        assertEquals(2, table.getRounds().size());
        assertEquals(5, table.getRound().getPlayers().size());
        assertEquals(table.getPlayer(3), table.getRound().getBigBlindPlayer());
        assertNull(table.getRound().getSmallBlindPlayer());
        assertEquals(table.getPlayer(1), table.getRound().getPositions().buttonPlayer());
    }

    @Test
    public void smallBlindLeavesInMiddleOfRoundAndOnNextRoundButtonDoesNotMove() {
        System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "0");
        default6PlayersJoin6MaxTable();
        table.leave(table.getRound().getSmallBlindPlayer().getId()); //SB leaves
        table.fold(table.getPlayer(3).getId()); //UTG
        table.fold(table.getPlayer(4).getId()); //UTG+1
        table.fold(table.getPlayer(5).getId()); //cutoff
        table.fold(table.getPlayer(0).getId()); //button
        //BB wins automatically
        assertEquals(new BigDecimal("805.00"), table.getPlayer(2).getCurrentBalance());
        waitRoundToStart();
        //Second round
        assertEquals(2, table.getRounds().size());
        assertEquals(5, table.getRound().getPlayers().size());
        assertEquals(table.getPlayer(3), table.getRound().getBigBlindPlayer());
        assertEquals(table.getPlayer(0), table.getRound().getPositions().buttonPlayer());
        assertEquals(0, table.getRound().getPositions().buttonSeatNumber());
        assertEquals(table.getPlayer(2), table.getRound().getSmallBlindPlayer());
    }

    @Test
    public void buttonPlayerLeavesInMiddleOfRoundAndButtonMovesToPreviousSmallBlindPlayer() {
        System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "0");
        default6PlayersJoin6MaxTable();
        table.leave(table.getRound().getPositions().buttonPlayer().getId()); //Button leaves from 0
        table.fold(table.getPlayer(3).getId()); //UTG
        table.fold(table.getPlayer(4).getId()); //UTG+1
        table.fold(table.getPlayer(5).getId()); //cutoff
        table.fold(table.getPlayer(1).getId()); // SB
        //BB wins automatically
        assertEquals(new BigDecimal("995.00"), table.getPlayer(1).getCurrentBalance());
        assertEquals(new BigDecimal("805.00"), table.getPlayer(2).getCurrentBalance());
        assertEquals(HoldemPhase.ROUND_COMPLETED, table.getGamePhase());
        waitRoundToStart();
        assertEquals(HoldemPhase.PRE_FLOP, table.getGamePhase());
        //Second round
        assertEquals(table.getPlayer(3), table.getRound().getBigBlindPlayer());
        assertEquals(table.getPlayer(1), table.getRound().getPositions().buttonPlayer());
        assertEquals(1, table.getRound().getPositions().buttonSeatNumber());
        assertEquals(table.getPlayer(2), table.getRound().getSmallBlindPlayer());
    }

    @Test
    public void bigAndSmallBlindPlayersLeaveDuringRoundAndButtonStaysAndNoSmallBlindPlayer() {
        System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "0");
        default6PlayersJoin6MaxTable();
        table.leave(table.getRound().getBigBlindPlayer().getId()); //BB Leaves
        table.leave(table.getRound().getSmallBlindPlayer().getId()); //SB Leaves
        table.raiseTo(table.getPlayer(3).getId(), new BigDecimal("22.47")); //UTG raiseTo 22.47
        table.fold(table.getPlayer(4).getId()); //UTG+1
        table.fold(table.getPlayer(5).getId()); //cutoff
        table.fold(table.getPlayer(0).getId()); // Button
        //SB and BB are autoFolded and new round starts
        assertEquals(new BigDecimal("715.00"), table.getPlayer(3).getCurrentBalance());
        assertNull(table.getPlayer(2));
        assertNull(table.getPlayer(1));
        assertEquals(HoldemPhase.ROUND_COMPLETED, table.getGamePhase());
        waitRoundToStart();
        assertEquals(HoldemPhase.PRE_FLOP, table.getGamePhase());
        //Second round
        assertEquals(4, table.getActivePlayerCount());
        assertEquals(4, table.getPlayerCount());
        assertEquals(table.getPlayer(3), table.getRound().getBigBlindPlayer());
        assertEquals(table.getPlayer(0), table.getRound().getPositions().buttonPlayer());
        assertEquals(0, table.getRound().getPositions().buttonSeatNumber());
        assertNull(table.getRound().getSmallBlindPlayer());
        assertNull(table.getPlayer(1));
        assertNull(table.getPlayer(2));
    }

    @Test
    public void multiplayerTableDiminishesToHeadsUpWhenPlayersLeaveButtonShouldMoveSeats() {
       /*
        Seat->  0		1		2		3		4		5
         1.		D/LF	SB/LF	BB		F		LF		LF
         2.		-		-		D/SB	BB      -       -
         3.		-       -       BB      D/SB    -       -

       */
        System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "0");
        default6PlayersJoin6MaxTable();
        table.fold(table.getPlayer(3).getId()); //UTG folds
        table.leave(table.getPlayer(4).getId()); //UTG plus1 leaves and folds
        table.leave(table.getPlayer(5).getId()); //Cutoff leaves and folds
        table.leave(table.getPlayer(0).getId()); //Button leaves and folds
        table.leave(table.getPlayer(1).getId()); //Sb leaves and folds
        //BB wins 5
        assertEquals(new BigDecimal("805.00"), table.getPlayer(2).getCurrentBalance());
        assertEquals(HoldemPhase.ROUND_COMPLETED, table.getGamePhase());
        waitRoundToStart();
        //Round 2 is heads up
        assertEquals(HoldemPhase.PRE_FLOP, table.getGamePhase());
        //Second round
        assertEquals(PlayerStatus.ACTIVE, table.getPlayer(3).getStatus());
        assertEquals(PlayerStatus.ACTIVE, table.getPlayer(2).getStatus());
        assertEquals(table.getPlayer(3), table.getRound().getBigBlindPlayer());
        assertEquals(table.getPlayer(2), table.getRound().getSmallBlindPlayer());
        assertEquals(2, table.getRound().getPositions().buttonSeatNumber());
        assertEquals(table.getPlayer(2), table.getRound().getPositions().buttonPlayer());
        table.fold(table.getRound().getSmallBlindPlayer().getId());
        //Round 3 is heads up
        waitRoundToStart();
        assertEquals(PlayerStatus.ACTIVE, table.getPlayer(3).getStatus());
        assertEquals(PlayerStatus.ACTIVE, table.getPlayer(2).getStatus());
        assertEquals(table.getPlayer(2), table.getRound().getBigBlindPlayer());
        assertEquals(table.getPlayer(3), table.getRound().getSmallBlindPlayer());
        assertEquals(3, table.getRound().getPositions().buttonSeatNumber());
        assertEquals(table.getPlayer(3), table.getRound().getPositions().buttonPlayer());
    }

    @Test
    public void newRoundStartsWithTwoPlayersAndButtonStays() {
       /*
       Seat->   0		1		2		3		4		5
         1.		SB/L	BB/L    UTG/LF	CA		LF		D/F
         1.2    -       CH      -       B       -       -
         1.3    -       F       -       -       -       -
         2		-		-		-		BB      -		D/SB
       */
        System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "5");
        default6PlayersJoin6MaxTable();
        //1.1
        table.leave(table.getPlayer(2).getId()); //UTG leaves and folds
        table.call(table.getPlayer(3).getId()); //UTG plus1 calls
        table.leave(table.getActivePlayer().getId()); //Cutoff leaves and folds
        table.fold(table.getActivePlayer().getId()); //Button folds
        table.leave(table.getPlayer(0).getId()); //Sb leaves and folds
        table.leave(table.getPlayer(1).getId()); //Bb leaves and sets autoplay
        assertEquals(2, table.getRound().getPlayers().stream().filter(PokerPlayer::hasHoleCards).count());
        assertEquals(HoldemPhase.FLOP, table.getGamePhase());
        //1.2
        //BB autochecks first by autoplay
        //UTG+1 bets
        table.raiseTo(table.getPlayer(3).getId(), new BigDecimal("21.01")); //UTG+1 bets
        //1.3
        //BB autofolds
        assertEquals(1, table.getRound().getPlayers().stream().filter(PokerPlayer::hasHoleCards).count());
        assertEquals(PlayerStatus.ACTIVE, table.getPlayer(5).getStatus());
        assertEquals(PlayerStatus.ACTIVE, table.getPlayer(3).getStatus());
        waitRoundToStart();
        assertEquals(PlayerStatus.ACTIVE, table.getPlayer(5).getStatus());
        assertEquals(PlayerStatus.ACTIVE, table.getPlayer(3).getStatus());
        assertNull(table.getPlayer(1));
        assertNull(table.getPlayer(2));
        assertNull(table.getPlayer(4));
        assertEquals(HoldemPhase.PRE_FLOP, table.getGamePhase());
        assertEquals(table.getRound().getSmallBlindPlayer(), table.getPlayer(5));
        assertEquals(table.getRound().getPositions().buttonPlayer(), table.getPlayer(5));
        assertEquals(table.getRound().getPositions().bb(), table.getPlayer(3));
        assertEquals(2, table.getRound().getPlayers().size());
    }

    @Test
    public void positionsAreOrganizedWhenSmallBlindLeavesAndBigBlindSitsOutNextHand() {
       /*
       Seat->   0		1		2		3		4		5
         1.		SB/L	BB/F    UTG/LF	CA		LF		D/F
         2		-		-		BB		x      -		D
       */
        System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "5");
        default6PlayersJoin6MaxTable();
        //1.1
        table.fold(table.getPlayer(2).getId()); //UTG folds
        table.allIn(table.getPlayer(3).getId()); //UTG plus1 allIn
        table.leave(table.getActivePlayer().getId()); //Cutoff leaves and folds
        table.fold(table.getActivePlayer().getId()); //Button folds
        table.sitOutNextHand(table.getPlayer(1).getId()); //BB sitsOut for next hand
        table.leave(table.getPlayer(0).getId()); //Sb leaves and folds
        table.fold(table.getActivePlayer().getId());    //BB folds
        waitRoundToStart();
        assertEquals(HoldemPhase.PRE_FLOP, table.getGamePhase());
        assertEquals(3, table.getRound().getPlayers().size());
        assertNull(table.getRound().getSmallBlindPlayer());
        assertEquals(table.getRound().getPositions().buttonPlayer(), table.getPlayer(5));
        assertEquals(table.getRound().getPositions().bb(), table.getPlayer(2));
    }

    @Test
    public void buttonAndSmallAndBigBlindPlayersLeaveAndButtonStaysAtEmptySeat() {
       /*
       Seat->   0		1		2		3		4		5
         1.		SB/LF	BB/LF   UTG/AI	F		F		D/LF
         2		-		-		BB		x       x		D/-
       */
        System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "5");
        default6PlayersJoin6MaxTable();
        //1.1
        table.allIn(table.getPlayer(2).getId()); //UTG AllIn
        table.fold(table.getPlayer(3).getId()); //UTG plus1 fold
        table.fold(table.getActivePlayer().getId()); //Cutoff leaves and folds
        table.leave(table.getActivePlayer().getId()); //Button leaves/folds
        table.leave(table.getPlayer(0).getId()); //Sb leaves and folds
        table.leave(table.getActivePlayer().getId());    //BB leaves and folds
        waitRoundToStart();
        assertEquals(3, table.getActivePlayerCount());
        assertEquals(0, table.getPlayersCountWithStatus(PlayerStatus.LEFT));
        assertEquals(HoldemPhase.PRE_FLOP, table.getGamePhase());
        assertEquals(3, table.getRound().getPlayers().size());
        assertNull(table.getRound().getSmallBlindPlayer());
        assertNull(table.getRound().getPositions().buttonPlayer());
        assertEquals(table.getPlayer(2), table.getRound().getPositions().bb());
        assertEquals(5, table.getRound().getPositions().buttonSeatNumber());
    }

    @Test
    public void lastPlayerLeavesAndAutoplayCompletesRound() {
        System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "5");
        default6PlayersJoin6MaxTable();
        //1.1
        table.allIn(table.getPlayer(2).getId()); //UTG AllIn
        table.fold(table.getPlayer(3).getId()); //UTG plus1 fold
        table.fold(table.getActivePlayer().getId()); //Cutoff leaves and folds
        table.leave(table.getActivePlayer().getId()); //Button leaves/folds
        table.leave(table.getPlayer(0).getId()); //Sb leaves and folds
        table.leave(table.getActivePlayer().getId());    //BB leaves and folds
        waitRoundToStart();
        assertEquals(3, table.getActivePlayerCount());
        assertEquals(0, table.getSitOutPlayerCount());
        assertEquals(0, table.getPlayersCountWithStatus(PlayerStatus.LEFT));
        assertEquals(HoldemPhase.PRE_FLOP, table.getGamePhase());
        assertEquals(3, table.getRound().getPlayers().size());
        assertNull(table.getRound().getSmallBlindPlayer());
        assertNull(table.getRound().getPositions().buttonPlayer());
        assertNull(table.getPlayer(0));
        assertEquals(table.getPlayer(2), table.getRound().getPositions().bb());
        assertEquals(5, table.getRound().getPositions().buttonSeatNumber());
    }
    @Test
    public void remowingPlayerWhenTableHasNotYetStartedUpdatePlayerCount() {
       table.join(bridge, "5", false);
       assertEquals(1, table.getNewPlayerCount());
       assertEquals(1, table.getPlayers().size());
       table.leave(bridge.userId());
       assertEquals(0, table.getPlayers().size());
       assertEquals(0, table.getNewPlayerCount());
    }

}