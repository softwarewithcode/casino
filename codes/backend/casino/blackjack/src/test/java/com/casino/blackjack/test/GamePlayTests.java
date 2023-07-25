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

import com.casino.blackjack.dealer.BlackjackDealer;
import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.cards.Card;
import com.casino.common.cards.Suit;
import com.casino.common.user.User;

public class GamePlayTests extends BaseTest {
	private BlackjackTable table;
	private BlackjackDealer dealer;

	@BeforeEach
	public void initTest() {
		try {
			table = new BlackjackTable(getDefaultTableInitData(), blackjackInitData);
			user = new User("JohnDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
			user2 = new User("JaneDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
			user3 = new User("JohnDoe2", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
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
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		// dealer.dealInitialCards();
		assertEquals(5, table.getPlayer(user.userId()).getHands().get(0).getCards().get(0).getRank());
		assertEquals(Suit.SPADE, table.getPlayer(user.userId()).getHands().get(0).getCards().get(0).getSuit());
		assertEquals(9, table.getPlayer(user.userId()).getHands().get(0).getCards().get(1).getRank());
		assertEquals(Suit.DIAMOND, table.getPlayer(user.userId()).getHands().get(0).getCards().get(1).getSuit());
		BlackjackPlayer p = (BlackjackPlayer) table.getPlayer(user.userId());
		assertTrue(p.canTake());
		table.hit(user.userId());
		assertEquals(3, table.getPlayer(user.userId()).getHands().get(0).getCards().size());
		assertEquals(8, table.getPlayer(user.userId()).getHands().get(0).getCards().get(2).getRank());
		assertEquals(Suit.DIAMOND, table.getPlayer(user.userId()).getHands().get(0).getCards().get(2).getSuit());
		assertEquals(22, table.getPlayer(user.userId()).getHands().get(0).calculateValues().get(0));
		assertFalse(p.canTake());
	}

	@Test
	public void playerCannotTakeCardsWhenHandValueIs21() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(4, Suit.CLUB));
		cards.add(Card.of(7, Suit.HEART));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.HEART));
		cards.add(Card.of(5, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		// dealer.dealInitialCards();
		assertEquals(5, table.getPlayer(user.userId()).getHands().get(0).getCards().get(0).getRank());
		assertEquals(Suit.SPADE, table.getPlayer(user.userId()).getHands().get(0).getCards().get(0).getSuit());
		assertEquals(9, table.getPlayer(user.userId()).getHands().get(0).getCards().get(1).getRank());
		assertEquals(Suit.DIAMOND, table.getPlayer(user.userId()).getHands().get(0).getCards().get(1).getSuit());
		BlackjackPlayer p = (BlackjackPlayer) table.getPlayer(user.userId());
		assertTrue(p.canTake());
		table.hit(user.userId());
		assertEquals(3, table.getPlayer(user.userId()).getHands().get(0).getCards().size());
		assertEquals(7, table.getPlayer(user.userId()).getHands().get(0).getCards().get(2).getRank());
		assertEquals(Suit.HEART, table.getPlayer(user.userId()).getHands().get(0).getCards().get(2).getSuit());
		assertFalse(p.canTake());
		assertEquals(21, table.getPlayer(user.userId()).getHands().get(0).calculateValues().get(0));
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
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(1, table.getPlayer(user.userId()).getHands().size()); // 1 hand
		assertEquals(2, table.getPlayer(user.userId()).getHands().get(0).calculateValues().size());
		assertEquals(6, table.getPlayer(user.userId()).getHands().get(0).calculateValues().get(0));
		assertEquals(16, table.getPlayer(user.userId()).getHands().get(0).calculateValues().get(1));
		table.hit(user.userId());
		assertEquals(7, table.getPlayer(user.userId()).getHands().get(0).calculateValues().get(0));
		assertEquals(17, table.getPlayer(user.userId()).getHands().get(0).calculateValues().get(1));
		table.hit(user.userId());
		assertEquals(10, table.getPlayer(user.userId()).getHands().get(0).calculateValues().get(0));
		assertEquals(20, table.getPlayer(user.userId()).getHands().get(0).calculateValues().get(1));
		table.hit(table.getPlayer(user.userId()).getId());
		assertEquals(1, table.getPlayer(user.userId()).getHands().get(0).calculateValues().size());
		assertEquals(14, table.getPlayer(user.userId()).getHands().get(0).calculateValues().get(0));
		table.hit(user.userId());
		assertEquals(1, table.getPlayer(user.userId()).getHands().get(0).calculateValues().size());
		assertEquals(22, table.getPlayer(user.userId()).getHands().get(0).calculateValues().get(0));
	}

	@Test // starting ace makes a second value if not blackjack
	public void dealerPicksUpBlackjackWhenFirstCardIsAce() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(12, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.HEART));
		cards.add(Card.of(1, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(21, table.getPlayer(user.userId()).getHands().get(0).calculateValues().get(1));
		assertTrue(table.getPlayer(user.userId()).getHands().get(0).isBlackjack());
	}

	@Test
	public void dealerPicksUpBlackjackWhenSecondCardIsAce() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.HEART));
		cards.add(Card.of(10, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(21, table.getPlayer(user.userId()).getHands().get(0).calculateValues().get(1));
		assertTrue(table.getPlayer(user.userId()).getHands().get(0).isBlackjack());
		BlackjackPlayer p = (BlackjackPlayer) table.getPlayer(user.userId());
		assertFalse(p.canTake());
	}

	@Test
	public void blackjackRequiresExactlyTwoCards() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertFalse(table.getPlayer(user.userId()).getHands().get(0).isBlackjack());
		assertEquals(12, table.getPlayer(user.userId()).getHands().get(0).calculateValues().get(0));
		table.hit(table.getPlayer(user.userId()).getId());
		assertFalse(table.getPlayer(user.userId()).getHands().get(0).isBlackjack());
		assertEquals(21, table.getPlayer(user.userId()).getHands().get(0).calculateValues().get(0));
		BlackjackPlayer p = (BlackjackPlayer) table.getPlayer(user.userId());
		assertFalse(p.canTake());
	}

	@Test
	public void onlyOnePlayerReactsOnTimeButAllWinsBecauseDealerGetsOver21() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(7, Suit.DIAMOND));
		cards.add(Card.of(8, Suit.DIAMOND));
		cards.add(Card.of(13, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.SPADE));
		cards.add(Card.of(11, Suit.DIAMOND));
		cards.add(Card.of(6, Suit.DIAMOND));// dealer
		cards.add(Card.of(6, Suit.HEART));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.SPADE));
		table.join(user, "0");
		table.join(user2, "2");
		table.join(user3, "6");
		table.bet(user.userId(), new BigDecimal("99.0"));
		table.bet(user2.userId(), new BigDecimal("10.0"));
		table.bet(user3.userId(), new BigDecimal("25.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.hit(user.userId());
		sleep(PLAYER_TIME_SECONDS * 3, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("1099.00"), table.getPlayer(user.userId()).getCurrentBalance());
		assertEquals(new BigDecimal("1010.00"), table.getPlayer(user2.userId()).getCurrentBalance());
		assertEquals(new BigDecimal("1025.00"), table.getPlayer(user3.userId()).getCurrentBalance());
	}

	@Test
	public void playerJoinsDuringBetPhaseAndDoesNotGetCardsAndHaveNoWinningChance() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(10, Suit.SPADE));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.DIAMOND));
		cards.add(Card.of(11, Suit.HEART));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("99.0"));
		table.join(user2, "2");
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.stand(user.userId());
		sleep(ONE_UNIT, ChronoUnit.SECONDS);
		assertEquals(0, table.getPlayer(user2.userId()).getHands().get(0).getCards().size());
		assertEquals(18, table.getDealerHand().calculateFinalValue());
		assertEquals(20, table.getPlayer(user.userId()).getHands().get(0).calculateFinalValue());
	}
}
