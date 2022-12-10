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

import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.blackjack.rules.BlackjackDealer;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.cards.Card;
import com.casino.common.cards.Suit;
import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.common.table.Status;
import com.casino.common.table.Thresholds;
import com.casino.common.table.Type;

public class StartingHandSplitTest extends BaseTest {
	private BlackjackTable table;
	private BlackjackPlayer blackjackPlayer;
	private BlackjackDealer dealer;

	@BeforeEach
	public void initTest() {
		try {
			table = new BlackjackTable(Status.WAITING_PLAYERS, new Thresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS, MIN_PLAYERS, MAX_PLAYERS, DEFAULT_SEAT_COUNT, Type.PUBLIC),
					UUID.randomUUID());
			blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
			Field f = table.getClass().getDeclaredField("dealer");
			f.setAccessible(true);
			dealer = (BlackjackDealer) f.get(table);
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
		table.join(5, blackjackPlayer);
		table.bet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(1, blackjackPlayer.getHands().size());
		table.split(blackjackPlayer);
		assertEquals(2, blackjackPlayer.getHands().size());
		assertTrue(blackjackPlayer.getHands().get(0).isActive());
		assertFalse(blackjackPlayer.getHands().get(1).isActive());
	}

	@Test
	public void onlyOneTimeSplitIsPossible() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.SPADE));
		table.join(5, blackjackPlayer);
		table.bet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(1, blackjackPlayer.getHands().size());
		table.split(blackjackPlayer);
		assertEquals(2, blackjackPlayer.getHands().size());
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.split(blackjackPlayer);
		});
	}

	@Test
	public void splitIsPossibleOnlyWith2Cards() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.SPADE));
		cards.add(Card.of(4, Suit.SPADE));
		table.join(5, blackjackPlayer);
		table.bet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.hit(blackjackPlayer);
		assertEquals(3, blackjackPlayer.getHands().get(0).getCards().size());
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.split(blackjackPlayer);
		});
	}

	@Test
	public void startingHandIsNotPossibleToSplitWithoutEqualValues() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(7, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.SPADE));
		table.join(5, blackjackPlayer);
		table.bet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.split(blackjackPlayer);
		});
	}

	@Test
	public void splitAddsAutomaticallyCardToFirstHand() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(9, Suit.SPADE));
		cards.add(Card.of(13, Suit.HEART));
		cards.add(Card.of(9, Suit.SPADE));
		table.join(5, blackjackPlayer);
		table.bet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(blackjackPlayer);
		assertEquals(2, blackjackPlayer.getActiveHand().getCards().size());
	}

	@Test
	public void splitDoesNotAddCardToSecondHand() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(12, Suit.DIAMOND));
		cards.add(Card.of(13, Suit.HEART));
		cards.add(Card.of(12, Suit.SPADE));
		table.join(5, blackjackPlayer);
		table.bet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(blackjackPlayer);
		assertEquals(1, blackjackPlayer.getHands().get(1).getCards().size());
	}

	@Test
	public void splitHandValuesAreCalculatedCorrectly() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(12, Suit.DIAMOND));
		cards.add(Card.of(13, Suit.HEART));
		cards.add(Card.of(12, Suit.SPADE));
		table.join(5, blackjackPlayer);
		table.bet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(blackjackPlayer);
		assertEquals(2, blackjackPlayer.getActiveHand().getCards().size());
		assertEquals(19, blackjackPlayer.getActiveHand().calculateValues().get(0));
		assertEquals(10, blackjackPlayer.getHands().get(1).calculateValues().get(0));
	}

	@Test
	public void takeCardAfterSplitAddsUpInFirstHand() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(13, Suit.HEART));
		cards.add(Card.of(3, Suit.SPADE));
		table.join(5, blackjackPlayer);
		table.bet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(blackjackPlayer);
		assertEquals(12, blackjackPlayer.getActiveHand().calculateValues().get(0));
		table.hit(blackjackPlayer);
		assertEquals(17, blackjackPlayer.getActiveHand().calculateValues().get(0));
		assertEquals(3, blackjackPlayer.getHands().get(1).calculateValues().get(0));
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
		table.join(5, blackjackPlayer);
		table.bet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(blackjackPlayer);
		assertEquals(12, blackjackPlayer.getActiveHand().calculateValues().get(0));
		table.hit(blackjackPlayer);
		assertEquals(17, blackjackPlayer.getActiveHand().calculateValues().get(0));
		assertEquals(3, blackjackPlayer.getHands().get(1).calculateValues().get(0));
		table.stand(blackjackPlayer);
		assertEquals(blackjackPlayer.getHands().get(1), blackjackPlayer.getActiveHand());
	}

	@Test
	public void takingCardOnSplittedHandAfterCallingStandAddsUpToSecondHand() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(11, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(13, Suit.HEART));
		cards.add(Card.of(3, Suit.SPADE));
		table.join(5, blackjackPlayer);
		table.bet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(blackjackPlayer);
		table.hit(blackjackPlayer);
		table.stand(blackjackPlayer);
		table.hit(blackjackPlayer);
		assertEquals(2, blackjackPlayer.getActiveHand().getCards().size());
		assertEquals(13, blackjackPlayer.getActiveHand().calculateValues().get(0));
	}

	@Test
	public void pictureCardsCanBeSplitted() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(11, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.HEART));
		cards.add(Card.of(12, Suit.SPADE));
		table.join(5, blackjackPlayer);
		table.bet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(blackjackPlayer);
		assertEquals(19, blackjackPlayer.getActiveHand().calculateValues().get(0));
		assertEquals(10, blackjackPlayer.getHands().get(1).calculateValues().get(0));
	}

	@Test
	public void splittingAcesCreatesTwoValues() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(10, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(13, Suit.HEART));
		cards.add(Card.of(1, Suit.SPADE));
		table.join(5, blackjackPlayer);
		table.bet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(blackjackPlayer);
		List<Integer> activeHandValues = blackjackPlayer.getActiveHand().calculateValues();
		assertEquals(2, activeHandValues.size());
		assertEquals(2, blackjackPlayer.getHands().get(1).calculateValues().size());
		assertEquals(10, activeHandValues.get(0));
		assertEquals(20, activeHandValues.get(1));
		assertEquals(1, blackjackPlayer.getHands().get(1).calculateValues().get(0));
		assertEquals(11, blackjackPlayer.getHands().get(1).calculateValues().get(1));
	}

	@Test
	public void splitAddsBetInHandAndBalanceKeepsOnTrack() {
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("100"), table);
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(11, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(13, Suit.HEART));
		cards.add(Card.of(3, Suit.SPADE));
		table.join(5, blackjackPlayer);
		table.bet(blackjackPlayer, new BigDecimal("6.77"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(blackjackPlayer);
		assertEquals(new BigDecimal("6.77"), blackjackPlayer.getHands().get(0).getBet());
		assertEquals(new BigDecimal("6.77"), blackjackPlayer.getHands().get(1).getBet());
		assertEquals(new BigDecimal("13.54"), blackjackPlayer.getTotalBet());
		assertEquals(new BigDecimal("86.46"), blackjackPlayer.getBalance());
	}

	@Test
	public void splitIsNotAllowedWithInsufficentBalance() {
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("100"), table);
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(11, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(13, Suit.HEART));
		cards.add(Card.of(3, Suit.SPADE));
		table.join(5, blackjackPlayer);
		table.bet(blackjackPlayer, new BigDecimal("50.1"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("49.90"), blackjackPlayer.getBalance());
		assertThrows(IllegalArgumentException.class, () -> {
			table.split(blackjackPlayer);
		});
	}

	@Test
	public void splitIsNotAllowedIfCardsHasBeenTaken() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(10, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.SPADE));
		table.join(5, blackjackPlayer);
		table.bet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.hit(blackjackPlayer);
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.split(blackjackPlayer);
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
		table.join(5, blackjackPlayer);
		table.bet(blackjackPlayer, new BigDecimal("10.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(blackjackPlayer);
		assertNull(blackjackPlayer.getActiveHand());
		assertEquals(2, blackjackPlayer.getHands().size());
		assertEquals(11, blackjackPlayer.getHands().get(0).getCards().get(0).getRank());
		assertEquals(1, blackjackPlayer.getHands().get(0).getCards().get(1).getRank());
		assertEquals(12, blackjackPlayer.getHands().get(1).getCards().get(0).getRank());
		assertEquals(1, blackjackPlayer.getHands().get(1).getCards().get(1).getRank());
		assertTrue(blackjackPlayer.getHands().get(0).isBlackjack());
		assertTrue(blackjackPlayer.getHands().get(1).isBlackjack());
		assertTrue(blackjackPlayer.getHands().get(0).isCompleted());
		assertTrue(blackjackPlayer.getHands().get(1).isCompleted());
		assertFalse(blackjackPlayer.getHands().get(0).isActive());
		assertFalse(blackjackPlayer.getHands().get(1).isActive());
	}
}
