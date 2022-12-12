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
import com.casino.common.cards.Card;
import com.casino.common.cards.Suit;
import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.common.table.Status;
import com.casino.common.table.Thresholds;
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
			table = new BlackjackTable(Status.WAITING_PLAYERS,
					new Thresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS, MIN_PLAYERS, MAX_PLAYERS, DEFAULT_SEAT_COUNT, Type.PUBLIC),
					UUID.randomUUID());
			blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
			blackjackPlayer2 = new BlackjackPlayer("JaneDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
			blackjackPlayer3 = new BlackjackPlayer("JaneDoe2", UUID.randomUUID(), new BigDecimal("1000"), table);
			Field f = table.getClass().getDeclaredField("dealer");
			f.setAccessible(true);
			dealer = (BlackjackDealer) f.get(table);
			dealer = (BlackjackDealer) f.get(table);
			List<Card> cards = dealer.getDecks();
			cards.add(Card.of(4, Suit.CLUB));
			cards.add(Card.of(8, Suit.DIAMOND));
			cards.add(Card.of(9, Suit.DIAMOND));
			cards.add(Card.of(9, Suit.HEART));
			cards.add(Card.of(5, Suit.SPADE));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void gamePhaseChangesFromBetToPlayAfterBetPhaseEnd() {
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		assertEquals(GamePhase.BET, table.getGamePhase());
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 1);
		assertEquals(GamePhase.BET, table.getGamePhase());
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(GamePhase.PLAY, table.getGamePhase());
	}

	@Test
	public void tableStatusChangesFromWaitingToRunningAfterFirstPlayerTakesSeat() {
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		assertTrue(table.getStatus() == Status.WAITING_PLAYERS);
		assertTrue(table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 1));
		assertTrue(table.getStatus() == Status.RUNNING);
	}

	@Test
	public void twoCardsIsAddedToStartingHandAfterPlacingBet() {
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 6);
		table.bet(blackjackPlayer.getId(), new BigDecimal("50.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(2, table.getPlayer(blackjackPlayer.getId()).getHands().get(0).getCards().size());
	}

	@Test
	public void playersBalanceGetAdjustedAccordingToBet() {
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		BlackjackPlayer blackjackPlayer2 = new BlackjackPlayer("JaneDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 1);
		table.join(blackjackPlayer2.getId(), blackjackPlayer2.getName(), blackjackPlayer2.getBalance(), 2);
		table.bet(blackjackPlayer.getId(), new BigDecimal("54.0"));
		table.bet(blackjackPlayer2.getId(), new BigDecimal("51.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("946.00"), table.getPlayer(blackjackPlayer.getId()).getBalance());
		assertEquals(new BigDecimal("949.00"), table.getPlayer(blackjackPlayer2.getId()).getBalance());
	}

	@Test
	public void onlyPlayerWithBetReceivesStartingHand() {
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		BlackjackPlayer blackjackPlayer2 = new BlackjackPlayer("JaneDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 1);
		table.join(blackjackPlayer2.getId(), blackjackPlayer2.getName(), blackjackPlayer2.getBalance(), 2);
		table.bet(blackjackPlayer.getId(), new BigDecimal("50.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS); // w for the dealer to complete. Just a number
		assertEquals(2, table.getPlayer(blackjackPlayer.getId()).getHands().get(0).getCards().size());
		assertEquals(0, table.getPlayer(blackjackPlayer2.getId()).getHands().get(0).getCards().size());
	}

	@Test
	public void dealerCreatesEightDecks() {
		Thresholds thresholds = new Thresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS, MIN_PLAYERS, MAX_PLAYERS, DEFAULT_SEAT_COUNT,
				Type.PUBLIC);
		BlackjackDealer d = new BlackjackDealer(null, thresholds);
		Assertions.assertEquals(416, d.getDecks().size());
	}

	@Test
	public void betChangeDoesNotMakeOnTimeAndLastReceivedBetIsUsed() {
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("100"), table);
		BlackjackPlayer blackjackPlayer2 = new BlackjackPlayer("JaneDoe", UUID.randomUUID(), new BigDecimal("100"), table);
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 1);
		table.join(blackjackPlayer2.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 2);
		table.bet(blackjackPlayer.getId(), new BigDecimal("11.11"));
		table.bet(blackjackPlayer2.getId(), new BigDecimal("22.67"));
		table.bet(blackjackPlayer.getId(), new BigDecimal("44.55"));
		table.bet(blackjackPlayer2.getId(), new BigDecimal("51.00"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.bet(blackjackPlayer2.getId(), new BigDecimal("99.0"));
		});
		assertEquals(new BigDecimal("44.55"), table.getPlayer(blackjackPlayer.getId()).getHands().get(0).getBet());
		assertEquals(new BigDecimal("44.55"), table.getPlayer(blackjackPlayer.getId()).getTotalBet());
		assertEquals(new BigDecimal("55.45"), table.getPlayer(blackjackPlayer.getId()).getBalance());
		assertEquals(new BigDecimal("51.00"), table.getPlayer(blackjackPlayer2.getId()).getHands().get(0).getBet());
		assertEquals(new BigDecimal("51.00"), table.getPlayer(blackjackPlayer2.getId()).getTotalBet());
		assertEquals(new BigDecimal("49.00"), table.getPlayer(blackjackPlayer2.getId()).getBalance());
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
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 0);
		table.join(blackjackPlayer2.getId(), blackjackPlayer2.getName(), blackjackPlayer2.getBalance(), 3);
		table.join(blackjackPlayer3.getId(), blackjackPlayer3.getName(), blackjackPlayer3.getBalance(), 6);
		table.bet(blackjackPlayer.getId(), new BigDecimal("11.11"));
		table.bet(blackjackPlayer2.getId(), new BigDecimal("22.67"));
		table.bet(blackjackPlayer3.getId(), new BigDecimal("44.55"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(2, table.getPlayer(blackjackPlayer.getId()).getActiveHand().getCards().get(0).getRank());
		assertEquals(6, table.getPlayer(blackjackPlayer.getId()).getActiveHand().getCards().get(1).getRank());
		assertEquals(3, table.getPlayer(blackjackPlayer2.getId()).getActiveHand().getCards().get(0).getRank());
		assertEquals(7, table.getPlayer(blackjackPlayer2.getId()).getActiveHand().getCards().get(1).getRank());
		assertEquals(4, table.getPlayer(blackjackPlayer3.getId()).getActiveHand().getCards().get(0).getRank());
		assertEquals(8, table.getPlayer(blackjackPlayer3.getId()).getActiveHand().getCards().get(1).getRank());
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
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 0);
		table.join(blackjackPlayer2.getId(), blackjackPlayer2.getName(), blackjackPlayer2.getBalance(), 3);
		table.join(blackjackPlayer3.getId(), blackjackPlayer3.getName(), blackjackPlayer3.getBalance(), 6);
		assertFalse(table.isDealerTurn());
		table.bet(blackjackPlayer.getId(), new BigDecimal("11.11"));
		table.bet(blackjackPlayer2.getId(), new BigDecimal("22.67"));
		table.bet(blackjackPlayer3.getId(), new BigDecimal("44.55"));
		assertFalse(table.isDealerTurn());
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(blackjackPlayer, table.getPlayerInTurn()); // equals compares UUID -> both have the same
		assertFalse(table.isDealerTurn());
		table.stand(blackjackPlayer.getId());
		assertFalse(table.isDealerTurn());
		assertEquals(blackjackPlayer2, table.getPlayerInTurn());
		assertFalse(table.isDealerTurn());
		table.stand(blackjackPlayer2.getId());
		assertFalse(table.isDealerTurn());
		assertEquals(blackjackPlayer3, table.getPlayerInTurn());
		table.stand(blackjackPlayer3.getId());
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
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 1);
		table.join(blackjackPlayer2.getId(), blackjackPlayer2.getName(), blackjackPlayer2.getBalance(), 3);
		table.join(blackjackPlayer3.getId(), blackjackPlayer3.getName(), blackjackPlayer3.getBalance(), 6);
		assertFalse(table.isDealerTurn());
		table.bet(blackjackPlayer.getId(), new BigDecimal("11.11"));
		table.bet(blackjackPlayer2.getId(), new BigDecimal("22.67"));
		table.bet(blackjackPlayer3.getId(), new BigDecimal("44.55"));
		assertFalse(table.isDealerTurn());
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(blackjackPlayer, table.getPlayerInTurn());
		assertFalse(table.isDealerTurn());
		table.hit(blackjackPlayer.getId());
		assertFalse(table.isDealerTurn());
		assertEquals(blackjackPlayer2, table.getPlayerInTurn());
		assertFalse(table.isDealerTurn());
		table.hit(blackjackPlayer2.getId());
		assertFalse(table.isDealerTurn());
		assertEquals(blackjackPlayer3, table.getPlayerInTurn());
		table.hit(blackjackPlayer3.getId());
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
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 1);
		table.join(blackjackPlayer2.getId(), blackjackPlayer2.getName(), blackjackPlayer2.getBalance(), 4);
		table.join(blackjackPlayer3.getId(), blackjackPlayer3.getName(), blackjackPlayer3.getBalance(), 5);
		assertFalse(table.isDealerTurn());
		table.bet(blackjackPlayer.getId(), new BigDecimal("11.11"));
		table.bet(blackjackPlayer2.getId(), new BigDecimal("22.67"));
		table.bet(blackjackPlayer3.getId(), new BigDecimal("44.55"));
		assertFalse(table.isDealerTurn());
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(blackjackPlayer, table.getPlayerInTurn());
		assertFalse(table.isDealerTurn());
		table.hit(blackjackPlayer.getId());
		assertFalse(table.isDealerTurn());
		assertEquals(blackjackPlayer2, table.getPlayerInTurn());
		assertFalse(table.isDealerTurn());
		table.hit(blackjackPlayer2.getId());
		assertFalse(table.isDealerTurn());
		assertEquals(blackjackPlayer3, table.getPlayerInTurn()); // equals compares UUID
		table.hit(blackjackPlayer3.getId());
		assertTrue(table.isDealerTurn());
		assertNull(table.getPlayerInTurn());
	}
}
