package com.casino.blackjack.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.blackjack.rules.BlackjackDealer;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.cards.Card;
import com.casino.common.cards.Suit;
import com.casino.common.table.Status;
import com.casino.common.table.Thresholds;
import com.casino.common.table.Type;

public class GamePlayTests extends BaseTest {
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
			blackjackPlayer2 = new BlackjackPlayer("JaneDoes", UUID.randomUUID(), new BigDecimal("1000"), table);
			blackjackPlayer3 = new BlackjackPlayer("JaneDoe2", UUID.randomUUID(), new BigDecimal("1000"), table);
			Field f = table.getClass().getDeclaredField("dealer");
			f.setAccessible(true);
			dealer = (BlackjackDealer) f.get(table);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void playerCannotTakeCardsIfHandValueIsOver21() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(4, Suit.CLUB));
		cards.add(Card.of(8, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.HEART));
		cards.add(Card.of(5, Suit.SPADE));
		table.join(5, blackjackPlayer);
		table.bet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		// dealer.dealInitialCards();
		assertEquals(5, blackjackPlayer.getHands().get(0).getCards().get(0).getRank());
		assertEquals(Suit.SPADE, blackjackPlayer.getHands().get(0).getCards().get(0).getSuit());
		assertEquals(9, blackjackPlayer.getHands().get(0).getCards().get(1).getRank());
		assertEquals(Suit.DIAMOND, blackjackPlayer.getHands().get(0).getCards().get(1).getSuit());
		assertTrue(blackjackPlayer.canTake());
		table.hit(blackjackPlayer);
		assertEquals(3, blackjackPlayer.getHands().get(0).getCards().size());
		assertEquals(8, blackjackPlayer.getHands().get(0).getCards().get(2).getRank());
		assertEquals(Suit.DIAMOND, blackjackPlayer.getHands().get(0).getCards().get(2).getSuit());
		assertEquals(22, blackjackPlayer.getHands().get(0).calculateValues().get(0));
		assertFalse(blackjackPlayer.canTake());
	}

	@Test
	public void playerCannotTakeCardsWhenHandValueIs21() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(4, Suit.CLUB));
		cards.add(Card.of(7, Suit.HEART));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.HEART));
		cards.add(Card.of(5, Suit.SPADE));
		table.join(5, blackjackPlayer);
		table.bet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		// dealer.dealInitialCards();
		assertEquals(5, blackjackPlayer.getHands().get(0).getCards().get(0).getRank());
		assertEquals(Suit.SPADE, blackjackPlayer.getHands().get(0).getCards().get(0).getSuit());
		assertEquals(9, blackjackPlayer.getHands().get(0).getCards().get(1).getRank());
		assertEquals(Suit.DIAMOND, blackjackPlayer.getHands().get(0).getCards().get(1).getSuit());
		assertTrue(blackjackPlayer.canTake());
		table.hit(blackjackPlayer);
		assertEquals(3, blackjackPlayer.getHands().get(0).getCards().size());
		assertEquals(7, blackjackPlayer.getHands().get(0).getCards().get(2).getRank());
		assertEquals(Suit.HEART, blackjackPlayer.getHands().get(0).getCards().get(2).getSuit());
		assertFalse(blackjackPlayer.canTake());
		assertEquals(21, blackjackPlayer.getHands().get(0).calculateValues().get(0));
	}

	@Test // starting ace makes a second value if not blackjack
	public void secondValueOfHandIsRemovedIfHandGoesOver21() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(8, Suit.DIAMOND));
		cards.add(Card.of(4, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.SPADE));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.HEART));
		cards.add(Card.of(1, Suit.SPADE));
		table.join(5, blackjackPlayer);
		table.bet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(1, blackjackPlayer.getHands().size()); // 1 hand
		assertEquals(2, blackjackPlayer.getHands().get(0).calculateValues().size());
		assertEquals(6, blackjackPlayer.getHands().get(0).calculateValues().get(0));
		assertEquals(16, blackjackPlayer.getHands().get(0).calculateValues().get(1));
		table.hit(blackjackPlayer);
		assertEquals(7, blackjackPlayer.getHands().get(0).calculateValues().get(0));
		assertEquals(17, blackjackPlayer.getHands().get(0).calculateValues().get(1));
		table.hit(blackjackPlayer);
		assertEquals(10, blackjackPlayer.getHands().get(0).calculateValues().get(0));
		assertEquals(20, blackjackPlayer.getHands().get(0).calculateValues().get(1));
		table.hit(blackjackPlayer);
		assertEquals(1, blackjackPlayer.getHands().get(0).calculateValues().size());
		assertEquals(14, blackjackPlayer.getHands().get(0).calculateValues().get(0));
		table.hit(blackjackPlayer);
		assertEquals(1, blackjackPlayer.getHands().get(0).calculateValues().size());
		assertEquals(22, blackjackPlayer.getHands().get(0).calculateValues().get(0));
		assertFalse(blackjackPlayer.canTake());
	}

	@Test // starting ace makes a second value if not blackjack
	public void dealerPicksUpBlackjackWhenFirstCardIsAce() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(12, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.HEART));
		cards.add(Card.of(1, Suit.SPADE));
		table.join(5, blackjackPlayer);
		table.bet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(21, blackjackPlayer.getHands().get(0).calculateValues().get(1));
		assertTrue(blackjackPlayer.getHands().get(0).isBlackjack());
		assertFalse(blackjackPlayer.canTake());
	}

	@Test
	public void dealerPicksUpBlackjackWhenSecondCardIsAce() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.HEART));
		cards.add(Card.of(10, Suit.SPADE));
		table.join(5, blackjackPlayer);
		table.bet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(21, blackjackPlayer.getHands().get(0).calculateValues().get(1));
		assertTrue(blackjackPlayer.getHands().get(0).isBlackjack());
		assertFalse(blackjackPlayer.canTake());
	}

	@Test
	public void blackjackRequiresExactlyTwoCards() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.SPADE));
		table.join(5, blackjackPlayer);
		table.bet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertFalse(blackjackPlayer.getHands().get(0).isBlackjack());
		assertEquals(12, blackjackPlayer.getHands().get(0).calculateValues().get(0));
		assertTrue(blackjackPlayer.canTake());
		table.hit(blackjackPlayer);
		assertFalse(blackjackPlayer.getHands().get(0).isBlackjack());
		assertEquals(21, blackjackPlayer.getHands().get(0).calculateValues().get(0));
		assertFalse(blackjackPlayer.canTake());
	}

	@Test
	public void playersDoNotReactOnTimeButWinBecauseDealerGetsOver21() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(9, Suit.SPADE));
		cards.add(Card.of(7, Suit.HEART));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.SPADE));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(6, Suit.DIAMOND));// dealer's first
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.SPADE));
		table.join(5, blackjackPlayer);
		table.join(6, blackjackPlayer2);
		table.join(2, blackjackPlayer3);
		table.bet(blackjackPlayer, new BigDecimal("99.0"));
		table.bet(blackjackPlayer2, new BigDecimal("10.0"));
		table.bet(blackjackPlayer3, new BigDecimal("25.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		sleep(PLAYER_TIME_SECONDS * 3, ChronoUnit.SECONDS); // 3 players waiting time +1 second
		assertEquals(new BigDecimal("1099.00"), blackjackPlayer.getBalance());
		assertEquals(new BigDecimal("1010.00"), blackjackPlayer2.getBalance());
		assertEquals(new BigDecimal("1025.00"), blackjackPlayer3.getBalance());
	}

	@Test
	public void splitHandGetsAdditionalCardAndCompletedWhenFirstHandIsActiveWhileTimingOut() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(9, Suit.SPADE));
		cards.add(Card.of(7, Suit.HEART));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.SPADE)); // dealer's second card
		cards.add(Card.of(1, Suit.DIAMOND)); // autoplay card for second hand
		cards.add(Card.of(6, Suit.DIAMOND)); // automatically added to first hand
		cards.add(Card.of(10, Suit.CLUB));
		cards.add(Card.of(8, Suit.DIAMOND));// dealer's first
		cards.add(Card.of(10, Suit.SPADE));
		table.join(5, blackjackPlayer);
		table.bet(blackjackPlayer, new BigDecimal("10.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(blackjackPlayer);
		sleep(PLAYER_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("20.00"), blackjackPlayer.getTotalBet());
		assertEquals(16, blackjackPlayer.getHands().get(0).getFinalValue());
		assertEquals(21, blackjackPlayer.getHands().get(1).getFinalValue());
		assertEquals(new BigDecimal("1005.00"), blackjackPlayer.getBalance());
	}

	@Test
	public void onlyOnePlayerReactsOnTimeButAllWinsBecauseDealerGetsOver21() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(7, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(13, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.SPADE));
		cards.add(Card.of(11, Suit.DIAMOND));
		cards.add(Card.of(6, Suit.DIAMOND));// dealer
		cards.add(Card.of(1, Suit.HEART));
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.SPADE));
		table.join(0, blackjackPlayer);
		table.join(2, blackjackPlayer2);
		table.join(6, blackjackPlayer3);
		table.bet(blackjackPlayer, new BigDecimal("99.0"));
		table.bet(blackjackPlayer2, new BigDecimal("10.0"));
		table.bet(blackjackPlayer3, new BigDecimal("25.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.hit(blackjackPlayer);
		sleep(PLAYER_TIME_SECONDS * 3 + 1, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("1099.00"), blackjackPlayer.getBalance());
		assertEquals(new BigDecimal("1015.00"), blackjackPlayer2.getBalance());
		assertEquals(new BigDecimal("1037.50"), blackjackPlayer3.getBalance());
	}

}
