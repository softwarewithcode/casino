package com.casino.poker.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.casino.common.player.PlayerStatus;
import com.casino.poker.game.HoldemPhase;
import com.casino.poker.round.positions.PokerPositionsBuilder;

public class SitOutTests extends DefaultTableTests {

	@Test
	public void utgSitsOutNextHandAndIsNotPlayingOnNextRound() {

		System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "5");
		default6PlayersJoin6MaxTable();
		table.call(table.getPlayer(2).getId());
		table.sitOutNextHand(table.getPlayer(2).getId());
		table.call(table.getPlayer(3).getId());
		table.allIn(table.getActivePlayer().getId());
		table.fold(table.getActivePlayer().getId());
		table.fold(table.getActivePlayer().getId());
		table.fold(table.getActivePlayer().getId());
		table.fold(table.getActivePlayer().getId());
		table.fold(table.getActivePlayer().getId());
		waitRoundToStart();
		assertEquals(5, table.getActivePlayerCount());
		assertEquals(5, table.getRound().getPlayers().size());
		assertEquals(1, table.getSitOutPlayerCount());
		assertEquals(PlayerStatus.SIT_OUT, table.getPlayer(2).getStatus());
		assertEquals(HoldemPhase.PRE_FLOP, table.getGamePhase());
	}

	@Test
	public void cutoffSitsOutNextHandAndIsNotPlayingOnNextRound() {

		System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "5");
		default6PlayersJoin6MaxTable();
		table.call(table.getPlayer(2).getId());
		table.call(table.getPlayer(3).getId());
		table.allIn(table.getActivePlayer().getId());
		table.sitOutNextHand(table.getPlayer(4).getId());
		table.fold(table.getActivePlayer().getId());
		table.fold(table.getActivePlayer().getId());
		table.fold(table.getActivePlayer().getId());
		table.fold(table.getActivePlayer().getId());
		table.fold(table.getActivePlayer().getId());
		waitRoundToStart();
		assertEquals(5, table.getActivePlayerCount());
		assertEquals(5, table.getRound().getPlayers().size());
		assertEquals(1, table.getSitOutPlayerCount());
		assertEquals(PlayerStatus.SIT_OUT, table.getPlayer(4).getStatus());
		assertEquals(HoldemPhase.PRE_FLOP, table.getGamePhase());
	}

	@Test
	public void dealerSitsOutNextHandAndIsNotPlayingOnNextRound() {

		System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "5");
		default6PlayersJoin6MaxTable();
		table.call(table.getPlayer(2).getId());
		table.call(table.getPlayer(3).getId());
		table.allIn(table.getActivePlayer().getId());
		table.sitOutNextHand(table.getPlayer(5).getId());
		table.fold(table.getActivePlayer().getId());
		table.fold(table.getActivePlayer().getId());
		;
		table.fold(table.getActivePlayer().getId());
		table.fold(table.getActivePlayer().getId());
		table.fold(table.getActivePlayer().getId());
		waitRoundToStart();
		assertEquals(5, table.getActivePlayerCount());
		assertEquals(5, table.getRound().getPlayers().size());
		assertEquals(1, table.getSitOutPlayerCount());
		assertEquals(PlayerStatus.SIT_OUT, table.getPlayer(5).getStatus());
		assertEquals(HoldemPhase.PRE_FLOP, table.getGamePhase());
	}

	@Test
	public void sbSitsOutNextHandAndIsNotPlayingOnNextRound() {
		System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "5");
		default6PlayersJoin6MaxTable();
		table.call(table.getPlayer(2).getId());
		table.call(table.getPlayer(3).getId());
		table.allIn(table.getActivePlayer().getId());
		table.sitOutNextHand(table.getPlayer(0).getId());
		table.fold(table.getActivePlayer().getId());
		table.fold(table.getActivePlayer().getId());
		table.fold(table.getActivePlayer().getId());
		table.fold(table.getActivePlayer().getId());
		table.fold(table.getActivePlayer().getId());
		waitRoundToStart();
		assertEquals(5, table.getActivePlayerCount());
		assertEquals(5, table.getRound().getPlayers().size());
		assertEquals(1, table.getSitOutPlayerCount());
		assertEquals(PlayerStatus.SIT_OUT, table.getPlayer(0).getStatus());
		assertEquals(HoldemPhase.PRE_FLOP, table.getGamePhase());
	}

	@Test
	public void bbSitsOutNextHandAndIsNotPlayingOnNextRound() {
		System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "5");
		default6PlayersJoin6MaxTable();
		table.call(table.getPlayer(2).getId());
		table.call(table.getPlayer(3).getId());
		table.allIn(table.getActivePlayer().getId());
		table.sitOutNextHand(table.getPlayer(1).getId());
		table.fold(table.getActivePlayer().getId());
		table.fold(table.getActivePlayer().getId());
		table.fold(table.getActivePlayer().getId());
		table.fold(table.getActivePlayer().getId());
		table.fold(table.getActivePlayer().getId());
		waitRoundToStart();
		assertEquals(5, table.getActivePlayerCount());
		assertEquals(5, table.getRound().getPlayers().size());
		assertEquals(1, table.getSitOutPlayerCount());
		assertEquals(PlayerStatus.SIT_OUT, table.getPlayer(1).getStatus());
		assertEquals(HoldemPhase.PRE_FLOP, table.getGamePhase());
	}

	@Test
	public void halfOfPlayersSitOutAndAreNotPlayingOnNextRound() {
		System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "5");
		default6PlayersJoin6MaxTable();
		table.sitOutNextHand(table.getPlayer(0).getId());
		table.call(table.getPlayer(2).getId());
		table.call(table.getPlayer(3).getId());
		table.allIn(table.getActivePlayer().getId());
		table.fold(table.getActivePlayer().getId());
		table.sitOutNextHand(table.getPlayer(3).getId());
		table.fold(table.getActivePlayer().getId());
		table.fold(table.getActivePlayer().getId());
		table.fold(table.getActivePlayer().getId());
		table.fold(table.getActivePlayer().getId());
		table.sitOutNextHand(table.getPlayer(4).getId());
		waitRoundToStart();
		assertEquals(3, table.getActivePlayerCount());
		assertEquals(3, table.getRound().getPlayers().size());
		assertEquals(3, table.getSitOutPlayerCount());
		assertEquals(PlayerStatus.SIT_OUT, table.getPlayer(0).getStatus());
		assertEquals(PlayerStatus.SIT_OUT, table.getPlayer(3).getStatus());
		assertEquals(PlayerStatus.SIT_OUT, table.getPlayer(4).getStatus());
		assertEquals(HoldemPhase.PRE_FLOP, table.getGamePhase());
	}

	@Test
	public void fourOutOfSixPlayersSitsOutAndRoundContinues() {
		System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "5");
		default6PlayersJoin6MaxTable();
		table.sitOutNextHand(table.getPlayer(0).getId());
		table.call(table.getPlayer(2).getId());
		table.call(table.getPlayer(3).getId());
		table.allIn(table.getActivePlayer().getId());
		table.fold(table.getActivePlayer().getId());
		table.sitOutNextHand(table.getPlayer(5).getId());
		table.sitOutNextHand(table.getPlayer(3).getId());
		table.fold(table.getActivePlayer().getId());
		table.fold(table.getActivePlayer().getId());
		table.fold(table.getActivePlayer().getId());
		table.fold(table.getActivePlayer().getId());
		table.sitOutNextHand(table.getPlayer(4).getId());
		waitRoundToStart();
		assertEquals(2, table.getActivePlayerCount());
		assertEquals(2, table.getRound().getPlayers().size());
		assertEquals(4, table.getSitOutPlayerCount());
		assertEquals(PlayerStatus.SIT_OUT, table.getPlayer(0).getStatus());
		assertEquals(PlayerStatus.SIT_OUT, table.getPlayer(3).getStatus());
		assertEquals(PlayerStatus.SIT_OUT, table.getPlayer(4).getStatus());
		assertEquals(PlayerStatus.SIT_OUT, table.getPlayer(5).getStatus());
		assertEquals(HoldemPhase.PRE_FLOP, table.getGamePhase());
	}

	@Test
	public void fourOutOfSixPlayersSitsOutButDecideToContinueGameDuringOnGoingRoundSoEveryBodyContinuesNextRound() {
		System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "5");
		default6PlayersJoin6MaxTable();
		table.sitOutNextHand(table.getPlayer(0).getId());
		table.continueGame(table.getPlayer(0).getId());
		table.call(table.getPlayer(2).getId());
		table.call(table.getPlayer(3).getId());
		table.allIn(table.getActivePlayer().getId());
		table.fold(table.getActivePlayer().getId());
		table.sitOutNextHand(table.getPlayer(5).getId());
		table.sitOutNextHand(table.getPlayer(3).getId());
		table.fold(table.getActivePlayer().getId());
		table.fold(table.getActivePlayer().getId());
		table.continueGame(table.getPlayer(3).getId());
		table.fold(table.getActivePlayer().getId());
		table.fold(table.getActivePlayer().getId());
		table.sitOutNextHand(table.getPlayer(4).getId());
		table.continueGame(table.getPlayer(5).getId());
		table.continueGame(table.getPlayer(4).getId());
		waitRoundToStart();
		assertEquals(6, table.getActivePlayerCount());
		assertEquals(6, table.getRound().getPlayers().size());
		assertEquals(0, table.getSitOutPlayerCount());
		assertEquals(HoldemPhase.PRE_FLOP, table.getGamePhase());
	}
	
    @Test
    public void buttonAndSmallAndBigBlindPlayersSitsOutNextHandAndButtonStaysAtEmptySeat() {
       /*
       Seat->   0		1		2		3		4		5
         1.		SB/LF	BB/LF   UTG/AI	F		F		D/LF
         2		-		-		BB		x       x		D/-
       */
        System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "5");
        default6PlayersJoin6MaxTable();
        table.sitOutNextHand(table.getRound().getBigBlindPlayer().getId());
        table.sitOutNextHand(table.getRound().getSmallBlindPlayer().getId());
        table.sitOutNextHand(table.getRound().getPositions().buttonPlayer().getId());
        table.allIn(table.getPlayer(2).getId()); //UTG AllIn
        table.fold(table.getPlayer(3).getId()); //UTG plus1 fold
        table.fold(table.getActivePlayer().getId()); //Cutoff leaves and folds
        table.fold(table.getActivePlayer().getId()); //Button folds
        table.fold(table.getPlayer(0).getId()); //Sb  folds
        table.fold(table.getActivePlayer().getId());	//BB folds  folds
        waitRoundToStart();
        assertEquals(3, table.getActivePlayerCount());
        assertEquals(3, table.getSitOutPlayerCount());
        assertEquals(HoldemPhase.PRE_FLOP, table.getGamePhase());
        assertEquals(3, table.getRound().getPlayers().size());
        assertNull(table.getRound().getSmallBlindPlayer());
        assertNull(table.getRound().getPositions().buttonPlayer());
        assertEquals(table.getPlayer(2), table.getRound().getPositions().bb());
        assertEquals(5, table.getRound().getPositions().buttonSeatNumber());
    }
	@Test
	public void newPlayerSitsOutAndDoesNotParticipateNextRound() {
		System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "2");
		defaultJoinJoin();
		table.join(bridge5, "4", false);
		table.sitOutNextHand(bridge5.userId());
		table.fold(table.getActivePlayer().getId());
		assertEquals(2, table.getRound().getPlayers().size());
		assertEquals(2, table.getActivePlayerCount());
		assertEquals(0, table.getSitOutPlayerCount());
		waitRoundToStart();
		assertEquals(2, table.getRound().getPlayers().size());
		assertEquals(2, table.getActivePlayerCount());
		assertEquals(1, table.getSitOutPlayerCount());
		assertEquals(2,table.getRound().getPlayers().size());
		assertEquals(table.getPlayer(3),table.getRound().getSmallBlindPlayer());
		assertEquals(table.getPlayer(3),table.getRound().getPositions().buttonPlayer());
		assertEquals(table.getPlayer(2),table.getRound().getBigBlindPlayer());
	}

	@Test
	public void newPlayerComesBackAndIsOnBigBlind() {
		System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "2");
		defaultJoinJoin();
		//Round1 => button = 2 = sb, bb = 3
		table.join(bridge5, "4", false);
		table.sitOutNextHand(bridge5.userId());
		table.fold(table.getActivePlayer().getId());
		waitRoundToStart();
		//Round2 => button = 3 = sb, bb = 2
		table.continueGame(bridge5.userId());
		table.fold(table.getActivePlayer().getId());
		assertEquals(table.getPlayer(3),table.getRound().getSmallBlindPlayer());
		assertEquals(table.getPlayer(3),table.getRound().getPositions().buttonPlayer());
		assertEquals(table.getPlayer(2),table.getRound().getBigBlindPlayer());
		waitRoundToStart();
		//Round3 => button = 2 = sb , bb=3
		assertEquals(2, table.getRound().getPlayers().size());
		assertEquals(1, table.getNewPlayerCount());
		assertEquals(2, table.getActivePlayerCount());
		assertEquals(0, table.getSitOutPlayerCount());
		assertEquals(table.getPlayer(2),table.getRound().getSmallBlindPlayer());
		assertEquals(table.getPlayer(2),table.getRound().getPositions().buttonPlayer());
		assertEquals(table.getPlayer(3),table.getRound().getBigBlindPlayer());
		assertEquals(2, table.getRound().getPositions().buttonSeatNumber());
		table.fold(table.getActivePlayer().getId());
		waitRoundToStart();
		//Round4 => button = 2 , sb=3, bb=4
		assertEquals(3, table.getRound().getPlayers().size());
		assertEquals(3, table.getActivePlayerCount());
		assertEquals(0, table.getSitOutPlayerCount());
		assertEquals(table.getPlayer(2),table.getRound().getPositions().buttonPlayer());
		assertEquals(table.getPlayer(3),table.getRound().getSmallBlindPlayer());
		assertEquals(table.getPlayer(4),table.getRound().getBigBlindPlayer());
		assertEquals(2, table.getRound().getPositions().buttonSeatNumber());
	}

	@Test
	public void firstJoinerSitsOutAndOthersBeginHeadsUpButSitoutPlayerComesBackAfterFirstRoundAndIsCompletedAndIsAcceptedToPlay() {
		System.getProperties().setProperty(PokerPositionsBuilder.BUTTON_POSITION_IN_TEST, "3");
		table.join(bridge2, "2", false);
		table.sitOutNextHand(bridge2.userId());
		table.join(bridge3, "3", false);
		table.join(bridge4, "4", false);
		waitRoundToStart();
		assertEquals(1, table.getRounds().size());
		assertEquals(2, table.getRound().getPlayers().size());
		assertEquals(2, table.getActivePlayerCount());
		assertEquals(1, table.getSitOutPlayerCount());
		assertEquals(table.getPlayer(3),table.getRound().getSmallBlindPlayer());
		assertEquals(table.getPlayer(4),table.getRound().getBigBlindPlayer());
		assertEquals(PlayerStatus.SIT_OUT_AS_NEW, table.getPlayer(2).getStatus());
		table.continueGame(bridge2.userId());
		table.fold(table.getActivePlayer().getId());
		assertEquals(HoldemPhase.ROUND_COMPLETED, table.getGamePhase());
		waitRoundToStart();
		assertEquals(table.getPlayer(4),table.getRound().getSmallBlindPlayer());
		assertEquals(table.getPlayer(2),table.getRound().getBigBlindPlayer());
		assertEquals(HoldemPhase.PRE_FLOP, table.getGamePhase());
		assertEquals(2, table.getRounds().size());
		assertEquals(3, table.getRound().getPlayers().size());
		assertEquals(3, table.getActivePlayerCount());
		assertEquals(0, table.getSitOutPlayerCount());
	}


}
