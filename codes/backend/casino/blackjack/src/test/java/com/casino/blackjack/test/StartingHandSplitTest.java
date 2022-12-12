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
			table = new BlackjackTable(Status.WAITING_PLAYERS,
					new Thresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS, MIN_PLAYERS, MAX_PLAYERS, DEFAULT_SEAT_COUNT, Type.PUBLIC),
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
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(1, blackjackPlayer.getHands().size());
		table.split(blackjackPlayer.getId());
		assertEquals(2, table.getPlayer(blackjackPlayer.getId()).getHands().size());
		assertTrue(table.getPlayer(blackjackPlayer.getId()).getHands().get(0).isActive());
		assertFalse(table.getPlayer(blackjackPlayer.getId()).getHands().get(1).isActive());
	}

	@Test
	public void onlyOneTimeSplitIsPossible() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.SPADE));
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(1, blackjackPlayer.getHands().size());
		table.split(blackjackPlayer.getId());
		assertEquals(2, table.getPlayer(blackjackPlayer.getId()).getHands().size());
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.split(blackjackPlayer.getId());
		});
	}

	@Test
	public void splitIsPossibleOnlyWith2Cards() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.SPADE));
		cards.add(Card.of(4, Suit.SPADE));
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.hit(blackjackPlayer.getId());
		assertEquals(3, table.getPlayer(blackjackPlayer.getId()).getHands().get(0).getCards().size());
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.split(blackjackPlayer.getId());
		});
	}

	@Test
	public void startingHandIsNotPossibleToSplitWithoutEqualValues() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(7, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.SPADE));
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.split(blackjackPlayer.getId());
		});
	}

	@Test
	public void splitAddsAutomaticallyCardToFirstHand() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(9, Suit.SPADE));
		cards.add(Card.of(13, Suit.HEART));
		cards.add(Card.of(9, Suit.SPADE));
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(blackjackPlayer.getId());
		assertEquals(2, table.getPlayer(blackjackPlayer.getId()).getActiveHand().getCards().size());
	}

	@Test
	public void splitDoesNotAddCardToSecondHand() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(12, Suit.DIAMOND));
		cards.add(Card.of(13, Suit.HEART));
		cards.add(Card.of(12, Suit.SPADE));
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(blackjackPlayer.getId());
		assertEquals(1, table.getPlayer(blackjackPlayer.getId()).getHands().get(1).getCards().size());
	}

	@Test
	public void splitHandValuesAreCalculatedCorrectly() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(12, Suit.DIAMOND));
		cards.add(Card.of(13, Suit.HEART));
		cards.add(Card.of(12, Suit.SPADE));
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(blackjackPlayer.getId());
		assertEquals(2, table.getPlayer(blackjackPlayer.getId()).getActiveHand().getCards().size());
		assertEquals(19, table.getPlayer(blackjackPlayer.getId()).getActiveHand().calculateValues().get(0));
		assertEquals(10, table.getPlayer(blackjackPlayer.getId()).getHands().get(1).calculateValues().get(0));
	}

	@Test
	public void takeCardAfterSplitAddsUpInFirstHand() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(13, Suit.HEART));
		cards.add(Card.of(3, Suit.SPADE));
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(blackjackPlayer.getId());
		assertEquals(12, table.getPlayer(blackjackPlayer.getId()).getActiveHand().calculateValues().get(0));
		table.hit(blackjackPlayer.getId());
		assertEquals(17, table.getPlayer(blackjackPlayer.getId()).getActiveHand().calculateValues().get(0));
		assertEquals(3, table.getPlayer(blackjackPlayer.getId()).getHands().get(1).calculateValues().get(0));
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
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(blackjackPlayer.getId());
		assertEquals(12, table.getPlayer(blackjackPlayer.getId()).getActiveHand().calculateValues().get(0));
		table.hit(blackjackPlayer.getId());
		assertEquals(17, table.getPlayer(blackjackPlayer.getId()).getActiveHand().calculateValues().get(0));
		assertEquals(3, table.getPlayer(blackjackPlayer.getId()).getHands().get(1).calculateValues().get(0));
		table.stand(blackjackPlayer.getId());
		assertEquals(table.getPlayer(blackjackPlayer.getId()).getHands().get(1), table.getPlayer(blackjackPlayer.getId()).getActiveHand());
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
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(blackjackPlayer.getId());
		table.hit(blackjackPlayer.getId());
		table.stand(blackjackPlayer.getId());
		table.hit(blackjackPlayer.getId());
		assertEquals(2, table.getPlayer(blackjackPlayer.getId()).getActiveHand().getCards().size());
		assertEquals(13, table.getPlayer(blackjackPlayer.getId()).getActiveHand().calculateValues().get(0));
	}

	@Test
	public void pictureCardsCanBeSplitted() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(11, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.HEART));
		cards.add(Card.of(12, Suit.SPADE));
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(blackjackPlayer.getId());
		assertEquals(19, table.getPlayer(blackjackPlayer.getId()).getActiveHand().calculateValues().get(0));
		assertEquals(10, table.getPlayer(blackjackPlayer.getId()).getHands().get(1).calculateValues().get(0));
	}

	@Test
	public void splittingAcesCreatesTwoValues() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(10, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(13, Suit.HEART));
		cards.add(Card.of(1, Suit.SPADE));
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(blackjackPlayer.getId());
		List<Integer> activeHandValues = table.getPlayer(blackjackPlayer.getId()).getActiveHand().calculateValues();
		assertEquals(2, activeHandValues.size());
		assertEquals(2, table.getPlayer(blackjackPlayer.getId()).getHands().get(1).calculateValues().size());
		assertEquals(10, activeHandValues.get(0));
		assertEquals(20, activeHandValues.get(1));
		assertEquals(1, table.getPlayer(blackjackPlayer.getId()).getHands().get(1).calculateValues().get(0));
		assertEquals(11, table.getPlayer(blackjackPlayer.getId()).getHands().get(1).calculateValues().get(1));
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
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("6.77"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(blackjackPlayer.getId());
		assertEquals(new BigDecimal("6.77"), table.getPlayer(blackjackPlayer.getId()).getHands().get(0).getBet());
		assertEquals(new BigDecimal("6.77"), table.getPlayer(blackjackPlayer.getId()).getHands().get(1).getBet());
		assertEquals(new BigDecimal("13.54"), table.getPlayer(blackjackPlayer.getId()).getTotalBet());
		assertEquals(new BigDecimal("86.46"), table.getPlayer(blackjackPlayer.getId()).getBalance());
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
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("50.1"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("49.90"), table.getPlayer(blackjackPlayer.getId()).getBalance());
		assertThrows(IllegalArgumentException.class, () -> {
			table.split(blackjackPlayer.getId());
		});
	}

	@Test
	public void splitIsNotAllowedIfCardsHasBeenTaken() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(10, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.SPADE));
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.hit(blackjackPlayer.getId());
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.split(blackjackPlayer.getId());
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
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("10.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.split(blackjackPlayer.getId());
		assertNull(table.getPlayer(blackjackPlayer.getId()).getActiveHand());
		assertEquals(2, table.getPlayer(blackjackPlayer.getId()).getHands().size());
		assertEquals(11, table.getPlayer(blackjackPlayer.getId()).getHands().get(0).getCards().get(0).getRank());
		assertEquals(1, table.getPlayer(blackjackPlayer.getId()).getHands().get(0).getCards().get(1).getRank());
		assertEquals(12, table.getPlayer(blackjackPlayer.getId()).getHands().get(1).getCards().get(0).getRank());
		assertEquals(1, table.getPlayer(blackjackPlayer.getId()).getHands().get(1).getCards().get(1).getRank());
		assertTrue(table.getPlayer(blackjackPlayer.getId()).getHands().get(0).isBlackjack());
		assertTrue(table.getPlayer(blackjackPlayer.getId()).getHands().get(1).isBlackjack());
		assertTrue(table.getPlayer(blackjackPlayer.getId()).getHands().get(0).isCompleted());
		assertTrue(table.getPlayer(blackjackPlayer.getId()).getHands().get(1).isCompleted());
		assertFalse(table.getPlayer(blackjackPlayer.getId()).getHands().get(0).isActive());
		assertFalse(table.getPlayer(blackjackPlayer.getId()).getHands().get(1).isActive());
	}
}
