package com.casino.blackjack.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.blackjack.rules.BlackjackDealer;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.bet.BetThresholds;
import com.casino.common.table.PlayerRange;
import com.casino.common.table.Status;
import com.casino.common.table.Type;
import com.casino.common.table.phase.GamePhase;

public class DealerTest extends BaseTest {
	@Test
	public void gamePhaseChangesFromBetToPlayAfterBetPhaseEnd() {
		BlackjackTable table = new BlackjackTable(Status.WAITING_PLAYERS, new BetThresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, PLAYER_TIME, INITIAL_DELAY), new PlayerRange(1, 7), Type.PUBLIC, 7, UUID.randomUUID());
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		assertEquals(GamePhase.BET, table.getGamePhase());
		table.trySeat(1, blackjackPlayer);
		assertEquals(GamePhase.BET, table.getGamePhase());
		table.placeStartingBet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(GamePhase.PLAY, table.getGamePhase());
	}

	@Test
	public void tableStatusChangesFromWaitingToRunningAfterFirstPlayerTakesSeat() {
		BlackjackTable table = new BlackjackTable(Status.WAITING_PLAYERS, new BetThresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, PLAYER_TIME, INITIAL_DELAY), new PlayerRange(1, 7), Type.PUBLIC, 7, UUID.randomUUID());
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		assertTrue(table.getStatus() == Status.WAITING_PLAYERS);
		assertTrue(table.trySeat(1, blackjackPlayer));
		assertTrue(table.getStatus() == Status.RUNNING);
	}

	@Test
	public void initialHandIsDealtAfterBetRoundHasEnded() {
		BlackjackTable table = new BlackjackTable(Status.WAITING_PLAYERS, new BetThresholds(MIN_BET, MAX_BET, 2, PLAYER_TIME, INITIAL_DELAY), new PlayerRange(1, 6), Type.PUBLIC, 15, UUID.randomUUID());
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("50"), table);
		table.trySeat(0, blackjackPlayer);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("49.9"));
		sleep(BET_ROUND_TIME_SECONDS + 2, ChronoUnit.SECONDS);
		BlackjackPlayer b = (BlackjackPlayer) blackjackPlayer;
		assertEquals(2, b.getHands().get(0).getCards().size());
	}

	@Test
	public void twoCardsIsAddedToStartingHandAfterPlacingBet() {
		BlackjackTable table = new BlackjackTable(Status.WAITING_PLAYERS, new BetThresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, PLAYER_TIME, INITIAL_DELAY), new PlayerRange(1, 7), Type.PUBLIC, 7, UUID.randomUUID());
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		assertEquals(1, blackjackPlayer.getHands().size());
		assertEquals(0, blackjackPlayer.getHands().get(0).getCards().size());
		table.trySeat(6, blackjackPlayer);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("50.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(2, blackjackPlayer.getHands().get(0).getCards().size());
	}

	@Test
	public void playersReceiveTwoCardsInFirstHandDuringInitialDeal() {
		BlackjackTable table = new BlackjackTable(Status.WAITING_PLAYERS, new BetThresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, PLAYER_TIME, INITIAL_DELAY), new PlayerRange(1, 7), Type.PUBLIC, 7, UUID.randomUUID());
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		BlackjackPlayer blackjackPlayer2 = new BlackjackPlayer("JaneDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		table.trySeat(1, blackjackPlayer);
		table.trySeat(2, blackjackPlayer2);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("54.0"));
		table.placeStartingBet(blackjackPlayer2, new BigDecimal("51.0"));
		assertEquals(1, blackjackPlayer.getHands().size());
		assertEquals(0, blackjackPlayer.getHands().get(0).getCards().size());
		assertEquals(1, blackjackPlayer2.getHands().size());
		assertEquals(0, blackjackPlayer2.getHands().get(0).getCards().size());
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS); //
		assertEquals(2, blackjackPlayer.getHands().get(0).getCards().size());
		assertEquals(2, blackjackPlayer2.getHands().get(0).getCards().size());
		assertEquals(1, blackjackPlayer.getHands().size());
		assertEquals(1, blackjackPlayer2.getHands().size());
	}

	@Test
	public void onlyPlayerWithBetReceivesStartingHand() {
		BlackjackTable table = new BlackjackTable(Status.WAITING_PLAYERS, new BetThresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, PLAYER_TIME, INITIAL_DELAY), new PlayerRange(1, 7), Type.PUBLIC, 7, UUID.randomUUID());
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		BlackjackPlayer blackjackPlayer2 = new BlackjackPlayer("JaneDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		assertTrue(table.trySeat(1, blackjackPlayer));
		assertTrue(table.trySeat(2, blackjackPlayer2));
		// var dealer = (BlackjackDealer) table.getDealer();
		table.placeStartingBet(blackjackPlayer, new BigDecimal("50.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS); // w for the dealer to complete. Just a number
		assertEquals(2, blackjackPlayer.getHands().get(0).getCards().size());
		assertEquals(0, blackjackPlayer2.getHands().get(0).getCards().size());
	}

	@Test
	public void dealerCreatesSixDecks() {
		BetThresholds betThresholds = new BetThresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, PLAYER_TIME, INITIAL_DELAY);
		BlackjackDealer d = new BlackjackDealer(null, betThresholds);
		Assertions.assertEquals(312, d.getDecks().size());
	}

}
