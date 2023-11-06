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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.casino.blackjack.dealer.Dealer;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.cards.Card;
import com.casino.common.cards.Suit;
import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.common.user.User;

public class StartingHandSplitTest extends BaseTest {
	private BlackjackTable table;
	private Dealer dealer;

	@BeforeEach
	public void initTest() {
		try {
			table = new BlackjackTable(getDefaultTableInitData(),blackjackInitData);
			user = new User("JohnDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
			user2 = new User("JohnDoe2", table.getId(), UUID.randomUUID(), null, new BigDecimal("100.0"));
			Field f = table.getClass().getDeclaredField("dealer");
			f.setAccessible(true);
			dealer = (Dealer) f.get(table);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void startingHandIsPossibleToSplitWithEqualRanks() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(13, Suit.HEART));
		cards.add(Card.of(5, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(1, table.getPlayer(user.userId()).getHands().size());
		table.split(user.userId());
		assertEquals(2, table.getPlayer(user.userId()).getHands().size());
		assertTrue(table.getPlayer(user.userId()).getHands().get(0).isActive());
		assertFalse(table.getPlayer(user.userId()).getHands().get(1).isActive());
	}

	@Test
	public void onlyOneTimeSplitIsPossible() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(1, table.getPlayer(user.userId()).getHands().size());
		table.split(user.userId());
		assertEquals(2, table.getPlayer(user.userId()).getHands().size());
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.split(user.userId());
		});
	}

	@Test
	public void splitIsPossibleOnlyWith2Cards() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.SPADE));
		cards.add(Card.of(4, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.hit(user.userId());
		assertEquals(3, table.getPlayer(user.userId()).getHands().get(0).getCards().size());
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.split(user.userId());
		});
	}

	@Test
	public void startingHandIsNotPossibleToSplitWithoutEqualValues() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(7, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.split(user.userId());
		});
	}

	@Test
	public void splitAddsAutomaticallyCardToFirstHand() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(9, Suit.SPADE));
		cards.add(Card.of(13, Suit.HEART));
		cards.add(Card.of(9, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(user.userId());
		assertEquals(2, table.getPlayer(user.userId()).getActiveHand().getCards().size());
	}

	@Test
	public void splitDoesNotAddCardToSecondHand() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(12, Suit.DIAMOND));
		cards.add(Card.of(13, Suit.HEART));
		cards.add(Card.of(12, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(user.userId());
		assertEquals(1, table.getPlayer(user.userId()).getHands().get(1).getCards().size());
	}

	@Test
	public void splitHandValuesAreCalculatedCorrectly() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(12, Suit.DIAMOND));
		cards.add(Card.of(13, Suit.HEART));
		cards.add(Card.of(12, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(user.userId());
		assertEquals(2, table.getPlayer(user.userId()).getActiveHand().getCards().size());
		assertEquals(19, table.getPlayer(user.userId()).getActiveHand().calculateValues().get(0));
		assertEquals(10, table.getPlayer(user.userId()).getHands().get(1).calculateValues().get(0));
	}

	@Test
	public void takeCardAfterSplitAddsUpInFirstHand() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(13, Suit.HEART));
		cards.add(Card.of(3, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(user.userId());
		assertEquals(12, table.getPlayer(user.userId()).getActiveHand().calculateValues().get(0));
		table.hit(user.userId());
		assertEquals(17, table.getPlayer(user.userId()).getActiveHand().calculateValues().get(0));
		assertEquals(3, table.getPlayer(user.userId()).getHands().get(1).calculateValues().get(0));
	}

	@Test
	public void callingStandOnSplitHandAfterTakingCardActivatesSecondHand() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(13, Suit.HEART));
		cards.add(Card.of(3, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(user.userId());
		assertEquals(12, table.getPlayer(user.userId()).getActiveHand().calculateValues().get(0));
		table.hit(user.userId());
		assertEquals(17, table.getPlayer(user.userId()).getActiveHand().calculateValues().get(0));
		assertEquals(3, table.getPlayer(user.userId()).getHands().get(1).calculateValues().get(0));
		table.stand(user.userId());
		assertEquals(table.getPlayer(user.userId()).getHands().get(1), table.getPlayer(user.userId()).getActiveHand());
	}

	@Test
	public void callingStandActivatesSecondHandAndCardsAreAddedToSecondHand() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(11, Suit.SPADE));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(13, Suit.HEART));
		cards.add(Card.of(3, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(user.userId());
		table.hit(user.userId());
		table.stand(user.userId());
		table.hit(user.userId());
		assertEquals(2, table.getPlayer(user.userId()).getHands().size());
		assertEquals(3, table.getPlayer(user.userId()).getActiveHand().getCards().size());
		assertEquals(15, table.getPlayer(user.userId()).getActiveHand().calculateValues().get(0));
	}

	@Test
	public void handsAreCompletedWhenSecondHandValueGoesOver21() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(12, Suit.DIAMOND));
		cards.add(Card.of(11, Suit.SPADE));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(13, Suit.HEART));
		cards.add(Card.of(3, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(user.userId());
		table.hit(user.userId());
		table.stand(user.userId());
		table.hit(user.userId());
		assertEquals(2, table.getPlayer(user.userId()).getHands().size());
		assertNull(table.getPlayer(user.userId()).getActiveHand());
		assertTrue(table.getPlayer(user.userId()).getHands().get(0).isCompleted());
		assertTrue(table.getPlayer(user.userId()).getHands().get(1).isCompleted());
	}

	@Test
	public void pictureCardsCanBeSplitted() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(11, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.HEART));
		cards.add(Card.of(12, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(user.userId());
		assertEquals(19, table.getPlayer(user.userId()).getActiveHand().calculateValues().get(0));
		assertEquals(10, table.getPlayer(user.userId()).getHands().get(1).calculateValues().get(0));
	}

	@Test
	public void splittingAcesCreatesTwoValues() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(10, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(13, Suit.HEART));
		cards.add(Card.of(1, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(user.userId());
		List<Integer> activeHandValues = table.getPlayer(user.userId()).getActiveHand().calculateValues();
		assertEquals(2, activeHandValues.size());
		assertEquals(2, table.getPlayer(user.userId()).getHands().get(1).calculateValues().size());
		assertEquals(10, activeHandValues.get(0));
		assertEquals(20, activeHandValues.get(1));
		assertEquals(1, table.getPlayer(user.userId()).getHands().get(1).calculateValues().get(0));
		assertEquals(11, table.getPlayer(user.userId()).getHands().get(1).calculateValues().get(1));
	}

	@Test
	public void splitAddsBetInHandAndBalanceKeepsOnTrack() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(11, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(13, Suit.HEART));
		cards.add(Card.of(3, Suit.SPADE));
		table.join(user2, "5");
		table.bet(user2.userId(), new BigDecimal("6.77"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(user2.userId());
		assertEquals(new BigDecimal("6.77"), table.getPlayer(user2.userId()).getHands().get(0).getBet());
		assertEquals(new BigDecimal("6.77"), table.getPlayer(user2.userId()).getHands().get(1).getBet());
		assertEquals(new BigDecimal("13.54"), table.getPlayer(user2.userId()).getTotalBet());
		assertEquals(new BigDecimal("86.46"), table.getPlayer(user2.userId()).getCurrentBalance());
	}

	@Test
	public void splitIsNotAllowedWithInsufficentBalance() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(11, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(13, Suit.HEART));
		cards.add(Card.of(3, Suit.SPADE));
		table.join(user2, "5");
		table.bet(user2.userId(), new BigDecimal("50.1"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("49.90"), table.getPlayer(user2.userId()).getCurrentBalance());
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.split(user2.userId());
		});
	}

	@Test
	public void splitIsNotAllowedIfCardsHasBeenTaken() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(10, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.hit(user.userId());
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.split(user.userId());
		});
	}

	@Test
	public void splitProducesTwoBlackjacksAndBothHandsAreCompleted() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(6, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(12, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.SPADE));
		cards.add(Card.of(11, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("10.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(user.userId());
		assertNull(table.getPlayer(user.userId()).getActiveHand());
		assertEquals(2, table.getPlayer(user.userId()).getHands().size());
		assertEquals(11, table.getPlayer(user.userId()).getHands().get(0).getCards().get(0).getRank());
		assertEquals(1, table.getPlayer(user.userId()).getHands().get(0).getCards().get(1).getRank());
		assertEquals(12, table.getPlayer(user.userId()).getHands().get(1).getCards().get(0).getRank());
		assertEquals(1, table.getPlayer(user.userId()).getHands().get(1).getCards().get(1).getRank());
		assertTrue(table.getPlayer(user.userId()).getHands().get(0).isBlackjack());
		assertTrue(table.getPlayer(user.userId()).getHands().get(1).isBlackjack());
		assertTrue(table.getPlayer(user.userId()).getHands().get(0).isCompleted());
		assertTrue(table.getPlayer(user.userId()).getHands().get(1).isCompleted());
		assertFalse(table.getPlayer(user.userId()).getHands().get(0).isActive());
		assertFalse(table.getPlayer(user.userId()).getHands().get(1).isActive());
	}

	@Test
	public void callingStandOnFirstHandAddsAutomaticallySecondCardToSecondHand() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(6, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(12, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.SPADE));
		cards.add(Card.of(11, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("10.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(20, table.getPlayer(user.userId()).getActiveHand().calculateValues().get(0));
		table.split(user.userId());
		table.stand(user.userId());
		assertEquals(15, table.getPlayer(user.userId()).getActiveHand().calculateValues().get(0));
	}

	@Test
	public void secondHandIsActivatedWhenFirstHandValueGoesOver21() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(6, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.DIAMOND));
		cards.add(Card.of(12, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.SPADE));
		cards.add(Card.of(11, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("10.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(20, table.getPlayer(user.userId()).getActiveHand().calculateValues().get(0));
		table.split(user.userId());
		table.hit(user.userId());
		assertEquals(25, table.getPlayer(user.userId()).getHands().get(0).calculateValues().get(0));
		assertTrue(table.getPlayer(user.userId()).getHands().get(1).isActive());
	}

	@Test
	public void bothHandsAreCompletedAfterTimingOut() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(6, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.SPADE));
		cards.add(Card.of(5, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("10.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(user.userId());
		sleep(PLAYER_TIME_SECONDS, ChronoUnit.SECONDS);
		assertTrue(table.getPlayer(user.userId()).getHands().get(0).isCompleted());
		assertTrue(table.getPlayer(user.userId()).getHands().get(1).isCompleted());
		assertNull(table.getPlayer(user.userId()).getActiveHand());
		assertEquals(15, table.getPlayer(user.userId()).getHands().get(0).calculateFinalValue());
		assertEquals(16, table.getPlayer(user.userId()).getHands().get(1).calculateFinalValue());
	}

	@Test
	public void bothHandsWinAfterTimeout() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(10, Suit.DIAMOND));
		cards.add(Card.of(6, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.SPADE));
		cards.add(Card.of(5, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("10.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(user.userId());
		sleep(PLAYER_TIME_SECONDS, ChronoUnit.SECONDS);
		assertTrue(table.getPlayer(user.userId()).getHands().get(0).isCompleted());
		assertTrue(table.getPlayer(user.userId()).getHands().get(1).isCompleted());
		assertNull(table.getPlayer(user.userId()).getActiveHand());
		assertEquals(new BigDecimal("1020.00"), table.getPlayer(user.userId()).getCurrentBalance());
	}

	@Test
	public void bothHandsLoseAferTimeout() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(10, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.DIAMOND));// dealer
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.SPADE));// dealer
		cards.add(Card.of(5, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("10.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(user.userId());
		sleep(PLAYER_TIME_SECONDS, ChronoUnit.SECONDS);
		assertTrue(table.getPlayer(user.userId()).getHands().get(0).isCompleted());
		assertTrue(table.getPlayer(user.userId()).getHands().get(1).isCompleted());
		assertNull(table.getPlayer(user.userId()).getActiveHand());
		assertEquals(new BigDecimal("980.00"), table.getPlayer(user.userId()).getCurrentBalance());
	}

	@Test
	public void firstHandWinsAfterTimeOut() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(10, Suit.DIAMOND));// dealer
		cards.add(Card.of(10, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.SPADE));// dealer
		cards.add(Card.of(10, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("10.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(user.userId());
		sleep(PLAYER_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("1005.00"), table.getPlayer(user.userId()).getCurrentBalance());
		assertTrue(table.getPlayer(user.userId()).getHands().get(0).isBlackjack());
	}

//	@Test
//	public void aceSplitGivesOnlyOneCardForEach() {
//		List<Card> cards = dealer.getDecks();
//		cards.add(Card.of(10, Suit.DIAMOND));// dealer
//		cards.add(Card.of(10, Suit.DIAMOND));
//		cards.add(Card.of(2, Suit.DIAMOND));
//		cards.add(Card.of(1, Suit.DIAMOND));
//		cards.add(Card.of(1, Suit.DIAMOND));
//		cards.add(Card.of(10, Suit.SPADE));// dealer
//		cards.add(Card.of(1, Suit.SPADE));
//		table.join(bridge, "5");
//		table.bet(bridge.userId(), new BigDecimal("10.0"));
//		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
//		table.split(bridge.userId());
//		assertTrue(table.getPlayer(bridge.userId()).getHands().get(0).isCompleted());
//		assertTrue(table.getPlayer(bridge.userId()).getHands().get(1).isCompleted());
//		assertEquals(12, table.getPlayer(bridge.userId()).getHands().get(0).calculateFinalValue());
//		assertEquals(new BigDecimal("1005.00"), table.getPlayer(bridge.userId()).getCurrentBalance());
//		assertTrue(table.getPlayer(bridge.userId()).getHands().get(0).isBlackjack());
//	}
}
