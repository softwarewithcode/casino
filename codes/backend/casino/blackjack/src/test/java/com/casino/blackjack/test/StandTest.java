package com.casino.blackjack.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.casino.blackjack.dealer.BlackjackDealer;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.cards.Card;
import com.casino.common.cards.Suit;
import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.common.user.User;

public class StandTest extends BaseTest {
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
	public void dealerGetsTurnAfterLastPlayerCallsStand() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(7, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.SPADE));
		table.join(user, "0");
		table.join(user2, "5");
		table.bet(user.userId(), new BigDecimal("99.0"));
		table.bet(user2.userId(), new BigDecimal("10.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.stand(user.userId());
		table.stand(user2.userId());
		assertTrue(table.isDealerTurn());
	}

	@Test
	public void callingStandInAWrongTurnResultsToException() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(7, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.SPADE));
		table.join(user, "5");
		table.join(user2, "6");
		table.bet(user.userId(), new BigDecimal("99.0"));
		table.bet(user2.userId(), new BigDecimal("10.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertThrows(IllegalPlayerActionException.class, () ->
			{
				table.stand(user2.userId());
			});
	}

	@Test
	public void callingStandWhenHandIsCompletedResultsToException() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(7, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.hit(user.userId());
		assertNull(table.getPlayer(user.userId()).getActiveHand());
		assertThrows(IllegalPlayerActionException.class, () ->
			{
				table.stand(user.userId());
			});
	}

	@Test
	public void callingStandWhileHavingBlackjackIsNotAllowedAsHandIsCompleted() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(7, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertNull(table.getPlayer(user.userId()).getActiveHand());
		assertThrows(IllegalPlayerActionException.class, () ->
			{
				table.stand(user.userId());
			});
	}

	@Test
	public void callingStandOnSecondHandOfSplitCompletesSecondHand() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(4, Suit.DIAMOND));
		cards.add(Card.of(4, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.SPADE));
		table.join(user, "0");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(user.userId());
		table.hit(user.userId());
		table.stand(user.userId());
		table.hit(user.userId());
		table.stand(user.userId());
		assertNull(table.getPlayer(user.userId()).getActiveHand());
		assertTrue(table.getPlayer(user.userId()).getHands().get(1).isCompleted());
	}

	@Test
	public void callingStandCausesDealerToPlayAndStandOn17() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.HEART));
		cards.add(Card.of(3, Suit.SPADE));
		table.join(user, "0");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.stand(user.userId());
		assertNull(table.getPlayer(user.userId()).getActiveHand());
		assertTrue(dealer.getHand().isCompleted());
		assertEquals(17, dealer.getHand().calculateValues().get(0));
	}

	@Test
	public void callingStandCausesDealerToCompleteRound() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.HEART));
		cards.add(Card.of(3, Suit.SPADE));
		table.join(user, "0");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.stand(user.userId());
		assertNull(table.getPlayer(user.userId()).getActiveHand());
		assertTrue(dealer.getHand().isCompleted());
		assertTrue(dealer.isRoundCompleted());
		assertEquals(17, dealer.getHand().calculateValues().get(0));
	}

	@Test
	public void callingStandCausesDealerToPlayAndStandOn18() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(4, Suit.DIAMOND));
		cards.add(Card.of(4, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(12, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.SPADE));
		table.join(user, "0");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.stand(user.userId());
		assertNull(table.getPlayer(user.userId()).getActiveHand());
		assertTrue(dealer.getHand().isCompleted());
		assertEquals(18, dealer.getHand().calculateValues().get(0));
	}

	@Test
	public void callingStandCausesDealerToPlayAndStandOn19() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(4, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(4, Suit.SPADE));
		cards.add(Card.of(4, Suit.SPADE));
		cards.add(Card.of(4, Suit.HEART));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(4, Suit.HEART));
		cards.add(Card.of(3, Suit.SPADE));
		table.join(user, "0");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.stand(user.userId());
		assertNull(table.getPlayer(user.userId()).getActiveHand());
		assertTrue(dealer.getHand().isCompleted());
		assertEquals(19, dealer.getHand().calculateValues().get(0));
	}

	@Test
	public void callingStandCausesDealerToPlayAndStandOn20() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(4, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.SPADE));
		cards.add(Card.of(3, Suit.SPADE));
		cards.add(Card.of(2, Suit.HEART));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(13, Suit.HEART));
		cards.add(Card.of(3, Suit.SPADE));
		table.join(user, "0");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.stand(user.userId());
		assertNull(table.getPlayer(user.userId()).getActiveHand());
		assertTrue(dealer.getHand().isCompleted());
		assertEquals(20, dealer.getHand().calculateValues().get(0));
	}

	@Test
	public void callingStandCausesDealerToPlayAndStandOn21() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.SPADE));
		cards.add(Card.of(3, Suit.SPADE));
		cards.add(Card.of(2, Suit.HEART));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(13, Suit.HEART));
		cards.add(Card.of(3, Suit.SPADE));
		table.join(user, "0");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.stand(user.userId());
		assertNull(table.getPlayer(user.userId()).getActiveHand());
		assertTrue(dealer.getHand().isCompleted());
		assertEquals(21, dealer.getHand().calculateValues().get(0));
	}
}
