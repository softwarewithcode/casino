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

import com.casino.blackjack.dealer.BlackjackDealer;
import com.casino.blackjack.game.BlackjackGamePhase;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.cards.Card;
import com.casino.common.cards.Suit;
import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.common.table.TableStatus;
import com.casino.common.user.User;

public class DealerTest extends BaseTest {
	private BlackjackTable table;
	private BlackjackDealer dealer;

	@BeforeEach
	public void initTest() {
		try {
			table = new BlackjackTable(getDefaultTableInitData(), blackjackInitData);
			Field f = table.getClass().getDeclaredField("dealer");
			f.setAccessible(true);
			dealer = (BlackjackDealer) f.get(table);
			dealer = (BlackjackDealer) f.get(table);
			user = new User("JohnDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
			user2 = new User("JaneDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
			user3 = new User("JohnDoe2", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
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
		assertEquals(BlackjackGamePhase.BET, table.getGamePhase());
		table.join(user, "1");
		assertEquals(BlackjackGamePhase.BET, table.getGamePhase());
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(BlackjackGamePhase.PLAY, table.getGamePhase());
	}

	@Test
	public void tableStatusChangesFromWaitingToRunningAfterFirstPlayerTakesSeat() {
		assertTrue(table.getStatus() == TableStatus.WAITING_PLAYERS);
		assertTrue(table.join(user, "1"));
		assertTrue(table.getStatus() == TableStatus.RUNNING);
	}

	@Test
	public void twoCardsIsAddedToStartingHandAfterPlacingBet() {
		table.join(user, "6");
		table.bet(user.userId(), new BigDecimal("50.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(2, table.getPlayer(user.userId()).getHands().get(0).getCards().size());
	}

	@Test
	public void playersBalanceGetAdjustedAccordingToBet() {
		table.join(user, "1");
		table.join(user2, "2");
		table.bet(user.userId(), new BigDecimal("54.0"));
		table.bet(user2.userId(), new BigDecimal("51.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("946.00"), table.getPlayer(user.userId()).getCurrentBalance());
		assertEquals(new BigDecimal("949.00"), table.getPlayer(user2.userId()).getCurrentBalance());
	}

	@Test
	public void onlyPlayerWithBetReceivesStartingHand() {
		table.join(user, "1");
		table.join(user2, "2");
		table.bet(user.userId(), new BigDecimal("50.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS); // w for the dealer to complete. Just a number
		assertEquals(2, table.getPlayer(user.userId()).getHands().get(0).getCards().size());
		assertEquals(0, table.getPlayer(user2.userId()).getHands().get(0).getCards().size());
	}

	@Test
	public void dealerCreatesEightDecks() {
		BlackjackDealer d = new BlackjackDealer(null, null);
		Assertions.assertEquals(416, d.getDecks().size());
	}

	@Test
	public void betChangeDoesNotMakeOnTimeAndLastReceivedBetIsUsed() {
		table.join(user, "1");
		table.join(user2, "2");
		table.bet(user.userId(), new BigDecimal("11.11"));
		table.bet(user2.userId(), new BigDecimal("22.67"));
		table.bet(user.userId(), new BigDecimal("44.55"));
		table.bet(user2.userId(), new BigDecimal("51.00"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.bet(user2.userId(), new BigDecimal("99.0"));
		});
		assertEquals(new BigDecimal("44.55"), table.getPlayer(user.userId()).getHands().get(0).getBet());
		assertEquals(new BigDecimal("44.55"), table.getPlayer(user.userId()).getTotalBet());
		assertEquals(new BigDecimal("955.45"), table.getPlayer(user.userId()).getCurrentBalance());
		assertEquals(new BigDecimal("51.00"), table.getPlayer(user2.userId()).getHands().get(0).getBet());
		assertEquals(new BigDecimal("51.00"), table.getPlayer(user2.userId()).getTotalBet());
		assertEquals(new BigDecimal("949.00"), table.getPlayer(user2.userId()).getCurrentBalance());
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
		table.join(user, "0");
		table.join(user2, "3");
		table.join(user3, "6");
		table.bet(user.userId(), new BigDecimal("11.11"));
		table.bet(user2.userId(), new BigDecimal("22.67"));
		table.bet(user3.userId(), new BigDecimal("44.55"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(2, table.getPlayer(user.userId()).getActiveHand().getCards().get(0).getRank());
		assertEquals(6, table.getPlayer(user.userId()).getActiveHand().getCards().get(1).getRank());
		assertEquals(3, table.getPlayer(user2.userId()).getActiveHand().getCards().get(0).getRank());
		assertEquals(7, table.getPlayer(user2.userId()).getActiveHand().getCards().get(1).getRank());
		assertEquals(4, table.getPlayer(user3.userId()).getActiveHand().getCards().get(0).getRank());
		assertEquals(8, table.getPlayer(user3.userId()).getActiveHand().getCards().get(1).getRank());
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
		table.join(user, "0");
		table.join(user2, "3");
		table.join(user3, "6");
		assertFalse(table.isDealerTurn());
		table.bet(user.userId(), new BigDecimal("11.11"));
		table.bet(user2.userId(), new BigDecimal("22.67"));
		table.bet(user3.userId(), new BigDecimal("44.55"));
		assertFalse(table.isDealerTurn());
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(user.userId(), table.getActivePlayer().getId()); // equals compares UUID -> both have the same
		assertFalse(table.isDealerTurn());
		table.stand(user.userId());
		assertFalse(table.isDealerTurn());
		assertEquals(user2.userId(), table.getActivePlayer().getId());
		assertFalse(table.isDealerTurn());
		table.stand(user2.userId());
		assertFalse(table.isDealerTurn());
		assertEquals(user3.userId(), table.getActivePlayer().getId());
		table.stand(user3.userId());
		assertTrue(table.isDealerTurn());
		assertNull(table.getActivePlayer());
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
		table.join(user, "1");
		table.join(user2, "3");
		table.join(user3, "6");
		assertFalse(table.isDealerTurn());
		table.bet(user.userId(), new BigDecimal("11.11"));
		table.bet(user2.userId(), new BigDecimal("22.67"));
		table.bet(user3.userId(), new BigDecimal("44.55"));
		assertFalse(table.isDealerTurn());
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(user.userId(), table.getActivePlayer().getId());
		assertFalse(table.isDealerTurn());
		table.hit(user.userId());
		assertFalse(table.isDealerTurn());
		assertEquals(user2.userId(), table.getActivePlayer().getId());
		assertFalse(table.isDealerTurn());
		table.hit(user2.userId());
		assertFalse(table.isDealerTurn());
		assertEquals(user3.userId(), table.getActivePlayer().getId());
		table.hit(user3.userId());
		assertTrue(table.isDealerTurn());
		assertNull(table.getActivePlayer());
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
		table.join(user, "1");
		table.join(user2, "4");
		table.join(user3, "5");
		assertFalse(table.isDealerTurn());
		table.bet(user.userId(), new BigDecimal("11.11"));
		table.bet(user2.userId(), new BigDecimal("22.67"));
		table.bet(user3.userId(), new BigDecimal("44.55"));
		assertFalse(table.isDealerTurn());
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(user.userId(), table.getActivePlayer().getId());
		assertFalse(table.isDealerTurn());
		table.hit(user.userId());
		assertFalse(table.isDealerTurn());
		assertEquals(user2.userId(), table.getActivePlayer().getId());
		assertFalse(table.isDealerTurn());
		table.hit(user2.userId());
		assertFalse(table.isDealerTurn());
		assertEquals(user3.userId(), table.getActivePlayer().getId()); // equals compares UUID
		table.hit(user3.userId());
		assertTrue(table.isDealerTurn());
		assertNull(table.getActivePlayer());
	}
}
