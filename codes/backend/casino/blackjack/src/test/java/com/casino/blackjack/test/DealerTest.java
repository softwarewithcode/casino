package com.casino.blackjack.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.blackjack.rules.BlackjackDealer;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.bet.BetThresholds;
import com.casino.common.cards.Card;
import com.casino.common.cards.Suit;
import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.common.table.PlayerRange;
import com.casino.common.table.Status;
import com.casino.common.table.Type;
import com.casino.common.table.phase.GamePhase;

public class DealerTest extends BaseTest {
	private BlackjackTable table;
	private BlackjackPlayer blackjackPlayer;
	private BlackjackPlayer blackjackPlayer2;
	private BlackjackPlayer blackjackPlayer3;
	private BlackjackDealer dealer;

	@BeforeEach
	public void initTest() {
		try {
			table = new BlackjackTable(Status.WAITING_PLAYERS, new BetThresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, PLAYER_TIME, INITIAL_DELAY), new PlayerRange(1, 7), Type.PUBLIC, 7, UUID.randomUUID());
			blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
			blackjackPlayer2 = new BlackjackPlayer("JaneDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
			blackjackPlayer3 = new BlackjackPlayer("JaneDoe2", UUID.randomUUID(), new BigDecimal("1000"), table);
			Field f = table.getClass().getDeclaredField("dealer");
			f.setAccessible(true);
			dealer = (BlackjackDealer) f.get(table);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

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

	@Test
	public void dealerCalculatesBalancesBasedOnLastBets() {
		BlackjackTable table = new BlackjackTable(Status.WAITING_PLAYERS, new BetThresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, PLAYER_TIME, INITIAL_DELAY), new PlayerRange(1, 7), Type.PUBLIC, 7, UUID.randomUUID());
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("100"), table);
		BlackjackPlayer blackjackPlayer2 = new BlackjackPlayer("JaneDoe", UUID.randomUUID(), new BigDecimal("100"), table);
		table.trySeat(5, blackjackPlayer);
		table.trySeat(6, blackjackPlayer2);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("11.11"));
		table.placeStartingBet(blackjackPlayer2, new BigDecimal("22.67"));
		table.placeStartingBet(blackjackPlayer, new BigDecimal("44.55"));
		table.placeStartingBet(blackjackPlayer2, new BigDecimal("51.00"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("44.55"), blackjackPlayer.getHands().get(0).getBet());
		assertEquals(new BigDecimal("44.55"), blackjackPlayer.getTotalBet());
		assertEquals(new BigDecimal("55.45"), blackjackPlayer.getBalance());
		assertEquals(new BigDecimal("51.00"), blackjackPlayer2.getHands().get(0).getBet());
		assertEquals(new BigDecimal("51.00"), blackjackPlayer2.getTotalBet());
		assertEquals(new BigDecimal("49.00"), blackjackPlayer2.getBalance());
	}

	@Test
	public void betChangeDoesNotMakeOnTimeAndLastReceivedBetIsUsed() {
		BlackjackTable table = new BlackjackTable(Status.WAITING_PLAYERS, new BetThresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, PLAYER_TIME, INITIAL_DELAY), new PlayerRange(1, 7), Type.PUBLIC, 7, UUID.randomUUID());
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("100"), table);
		BlackjackPlayer blackjackPlayer2 = new BlackjackPlayer("JaneDoe", UUID.randomUUID(), new BigDecimal("100"), table);
		table.trySeat(5, blackjackPlayer);
		table.trySeat(6, blackjackPlayer2);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("11.11"));
		table.placeStartingBet(blackjackPlayer2, new BigDecimal("22.67"));
		table.placeStartingBet(blackjackPlayer, new BigDecimal("44.55"));
		table.placeStartingBet(blackjackPlayer2, new BigDecimal("51.00"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.placeStartingBet(blackjackPlayer2, new BigDecimal("99.0"));
		});
		assertEquals(new BigDecimal("44.55"), blackjackPlayer.getHands().get(0).getBet());
		assertEquals(new BigDecimal("44.55"), blackjackPlayer.getTotalBet());
		assertEquals(new BigDecimal("55.45"), blackjackPlayer.getBalance());
		assertEquals(new BigDecimal("51.00"), blackjackPlayer2.getHands().get(0).getBet());
		assertEquals(new BigDecimal("51.00"), blackjackPlayer2.getTotalBet());
		assertEquals(new BigDecimal("49.00"), blackjackPlayer2.getBalance());
	}

	@Test
	public void dealerDealsInOrderWithMultiplePlayers() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(8, Suit.DIAMOND));
		cards.add(Card.of(7, Suit.DIAMOND));
		cards.add(Card.of(6, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.HEART));
		cards.add(Card.of(4, Suit.SPADE));
		cards.add(Card.of(3, Suit.HEART));
		cards.add(Card.of(2, Suit.SPADE));
		table.trySeat(0, blackjackPlayer);
		table.trySeat(3, blackjackPlayer2);
		table.trySeat(6, blackjackPlayer3);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("11.11"));
		table.placeStartingBet(blackjackPlayer2, new BigDecimal("22.67"));
		table.placeStartingBet(blackjackPlayer3, new BigDecimal("44.55"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(2, blackjackPlayer.getActiveHand().getCards().get(0).getRank());
		assertEquals(6, blackjackPlayer.getActiveHand().getCards().get(1).getRank());
		assertEquals(3, blackjackPlayer2.getActiveHand().getCards().get(0).getRank());
		assertEquals(7, blackjackPlayer2.getActiveHand().getCards().get(1).getRank());
		assertEquals(4, blackjackPlayer3.getActiveHand().getCards().get(0).getRank());
		assertEquals(8, blackjackPlayer3.getActiveHand().getCards().get(1).getRank());
		assertEquals(5, dealer.getHand().getCards().get(0).getRank());
		assertEquals(1, dealer.getHand().getCards().size());
	}

	@Test
	public void dealerChangesTurns() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(8, Suit.DIAMOND));
		cards.add(Card.of(7, Suit.DIAMOND));
		cards.add(Card.of(6, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.HEART));
		cards.add(Card.of(4, Suit.SPADE));
		cards.add(Card.of(3, Suit.HEART));
		cards.add(Card.of(2, Suit.SPADE));
		assertFalse(table.isDealerTurn());
		table.trySeat(0, blackjackPlayer);
		table.trySeat(3, blackjackPlayer2);
		table.trySeat(6, blackjackPlayer3);
		assertFalse(table.isDealerTurn());
		table.placeStartingBet(blackjackPlayer, new BigDecimal("11.11"));
		table.placeStartingBet(blackjackPlayer2, new BigDecimal("22.67"));
		table.placeStartingBet(blackjackPlayer3, new BigDecimal("44.55"));
		assertFalse(table.isDealerTurn());
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(blackjackPlayer, table.getPlayerInTurn());
		assertFalse(table.isDealerTurn());
		table.stand(blackjackPlayer);
		assertFalse(table.isDealerTurn());
		assertEquals(blackjackPlayer2, table.getPlayerInTurn());
		assertFalse(table.isDealerTurn());
		table.stand(blackjackPlayer2);
		assertFalse(table.isDealerTurn());
		assertEquals(blackjackPlayer3, table.getPlayerInTurn());
		table.stand(blackjackPlayer3);
		assertTrue(table.isDealerTurn());
		assertNull(table.getPlayerInTurn());
	}

	@Test
	public void dealerChangesTurnWhenPlayerGoesOver21() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(10, Suit.SPADE));
		cards.add(Card.of(10, Suit.SPADE));
		cards.add(Card.of(10, Suit.SPADE));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(4, Suit.HEART));
		cards.add(Card.of(10, Suit.SPADE));
		cards.add(Card.of(10, Suit.HEART));
		cards.add(Card.of(10, Suit.SPADE));
		assertFalse(table.isDealerTurn());
		table.trySeat(0, blackjackPlayer);
		table.trySeat(3, blackjackPlayer2);
		table.trySeat(6, blackjackPlayer3);
		assertFalse(table.isDealerTurn());
		table.placeStartingBet(blackjackPlayer, new BigDecimal("11.11"));
		table.placeStartingBet(blackjackPlayer2, new BigDecimal("22.67"));
		table.placeStartingBet(blackjackPlayer3, new BigDecimal("44.55"));
		assertFalse(table.isDealerTurn());
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(blackjackPlayer, table.getPlayerInTurn());
		assertFalse(table.isDealerTurn());
		table.takeCard(blackjackPlayer);
		assertFalse(table.isDealerTurn());
		assertEquals(blackjackPlayer2, table.getPlayerInTurn());
		assertFalse(table.isDealerTurn());
		table.takeCard(blackjackPlayer2);
		assertFalse(table.isDealerTurn());
		assertEquals(blackjackPlayer3, table.getPlayerInTurn());
		table.takeCard(blackjackPlayer3);
		assertTrue(table.isDealerTurn());
		assertNull(table.getPlayerInTurn());
	}

	@Test
	public void dealerChangesAfterPlayerDoublesDown() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(10, Suit.SPADE));
		cards.add(Card.of(10, Suit.SPADE));
		cards.add(Card.of(10, Suit.SPADE));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(4, Suit.HEART));
		cards.add(Card.of(10, Suit.SPADE));
		cards.add(Card.of(10, Suit.HEART));
		cards.add(Card.of(10, Suit.SPADE));
		assertFalse(table.isDealerTurn());
		table.trySeat(0, blackjackPlayer);
		table.trySeat(3, blackjackPlayer2);
		table.trySeat(6, blackjackPlayer3);
		assertFalse(table.isDealerTurn());
		table.placeStartingBet(blackjackPlayer, new BigDecimal("11.11"));
		table.placeStartingBet(blackjackPlayer2, new BigDecimal("22.67"));
		table.placeStartingBet(blackjackPlayer3, new BigDecimal("44.55"));
		assertFalse(table.isDealerTurn());
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(blackjackPlayer, table.getPlayerInTurn());
		assertFalse(table.isDealerTurn());
		table.takeCard(blackjackPlayer);
		assertFalse(table.isDealerTurn());
		assertEquals(blackjackPlayer2, table.getPlayerInTurn());
		assertFalse(table.isDealerTurn());
		table.takeCard(blackjackPlayer2);
		assertFalse(table.isDealerTurn());
		assertEquals(blackjackPlayer3, table.getPlayerInTurn());
		table.takeCard(blackjackPlayer3);
		assertTrue(table.isDealerTurn());
		assertNull(table.getPlayerInTurn());
	}
}
