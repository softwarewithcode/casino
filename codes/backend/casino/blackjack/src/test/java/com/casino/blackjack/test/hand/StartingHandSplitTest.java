package com.casino.blackjack.test.hand;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.casino.blackjack.external.IBlackjackTable;
import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.blackjack.rules.BlackjackDealer;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.blackjack.test.BaseTest;
import com.casino.common.bet.BetThresholds;
import com.casino.common.cards.Card;
import com.casino.common.cards.Suit;
import com.casino.common.exception.IllegalSplitException;
import com.casino.common.table.PlayerRange;
import com.casino.common.table.Status;
import com.casino.common.table.Type;

public class StartingHandSplitTest extends BaseTest {
	private IBlackjackTable table;
	private BlackjackPlayer blackjackPlayer;
	private BlackjackDealer dealer;

	@BeforeEach
	public void initTest() {
		try {
			table = new BlackjackTable(Status.WAITING_PLAYERS, new BetThresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, PLAYER_TIME, INITIAL_DELAY), new PlayerRange(1, 7), Type.PUBLIC, 7, UUID.randomUUID());
			blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), publicTable);
			blackjackPlayer2 = new BlackjackPlayer("JaneDoes", UUID.randomUUID(), new BigDecimal("1000"), publicTable);
			Field f = table.getClass().getDeclaredField("dealer");
			f.setAccessible(true);
			dealer = (BlackjackDealer) f.get(table);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void startingHandIsPossibleToSplitWithEqualRanks() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.SPADE));
		table.trySeat(5, blackjackPlayer);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(1, blackjackPlayer.getHands().size());
		table.splitStartingHand(blackjackPlayer);
		assertEquals(2, blackjackPlayer.getHands().size());
		assertTrue(blackjackPlayer.getHands().get(0).isActive());
		assertFalse(blackjackPlayer.getHands().get(1).isActive());
	}

	@Test
	public void onlyOneTimeSplitIsPossible() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.SPADE));
		table.trySeat(5, blackjackPlayer);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(1, blackjackPlayer.getHands().size());
		table.splitStartingHand(blackjackPlayer);
		assertEquals(2, blackjackPlayer.getHands().size());
		IllegalSplitException exception = assertThrows(IllegalSplitException.class, () -> {
			table.splitStartingHand(blackjackPlayer);
		});
		assertEquals(1, exception.getCode());
	}

	@Test
	public void splitInactiveHandIsNotPossible() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.SPADE));
		table.trySeat(5, blackjackPlayer);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		blackjackPlayer.getActiveHand().complete();
		IllegalSplitException exception = assertThrows(IllegalSplitException.class, () -> {
			table.splitStartingHand(blackjackPlayer);
		});
		assertEquals(2, exception.getCode());
	}

	@Test
	public void splitIsPossibleOnlyWith2Cards() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		table.trySeat(5, blackjackPlayer);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.takeCard(blackjackPlayer);
		assertEquals(3, blackjackPlayer.getHands().get(0).getCards().size());
		IllegalSplitException exception = assertThrows(IllegalSplitException.class, () -> {
			table.splitStartingHand(blackjackPlayer);
		});
		assertEquals(3, exception.getCode());
	}

	@Test
	public void startingHandIsNotPossibleToSplitWithoutEqualRanks() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.SPADE));
		table.trySeat(5, blackjackPlayer);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		IllegalSplitException exception = assertThrows(IllegalSplitException.class, () -> {
			table.splitStartingHand(blackjackPlayer);
		});
		assertEquals(4, exception.getCode());
	}

	@Test
	public void splitAddsAutomaticallyCardToFirstHand() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(9, Suit.SPADE));
		cards.add(Card.of(9, Suit.SPADE));
		table.trySeat(5, blackjackPlayer);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.splitStartingHand(blackjackPlayer);
		assertEquals(2, blackjackPlayer.getActiveHand().getCards().size());
	}

	@Test
	public void splitDoesNotAddCardToSecondHand() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(12, Suit.DIAMOND));
		cards.add(Card.of(12, Suit.SPADE));
		table.trySeat(5, blackjackPlayer);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.splitStartingHand(blackjackPlayer);
		assertEquals(1, blackjackPlayer.getHands().get(1).getCards().size());
	}

	@Test
	public void splitHandValuesAreCalculatedCorrectly() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(12, Suit.DIAMOND));
		cards.add(Card.of(12, Suit.SPADE));
		table.trySeat(5, blackjackPlayer);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.splitStartingHand(blackjackPlayer);
		assertEquals(2, blackjackPlayer.getActiveHand().getCards().size());
		assertEquals(19, blackjackPlayer.getActiveHand().calculateValues().get(0));
		assertEquals(10, blackjackPlayer.getHands().get(1).calculateValues().get(0));
	}

	@Test
	public void takeCardAfterSplitAddsUpInFirstHand() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.SPADE));
		table.trySeat(5, blackjackPlayer);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.splitStartingHand(blackjackPlayer);
		assertEquals(12, blackjackPlayer.getActiveHand().calculateValues().get(0));
		table.takeCard(blackjackPlayer);
		assertEquals(17, blackjackPlayer.getActiveHand().calculateValues().get(0));
		assertEquals(3, blackjackPlayer.getHands().get(1).calculateValues().get(0));
	}

	@Test
	public void callingStandOnSplitHandAfterTakingCardActivatesSecondHand() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.SPADE));
		table.trySeat(5, blackjackPlayer);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.splitStartingHand(blackjackPlayer);
		assertEquals(12, blackjackPlayer.getActiveHand().calculateValues().get(0));
		table.takeCard(blackjackPlayer);
		assertEquals(17, blackjackPlayer.getActiveHand().calculateValues().get(0));
		assertEquals(3, blackjackPlayer.getHands().get(1).calculateValues().get(0));
		table.stand(blackjackPlayer);
		assertEquals(blackjackPlayer.getHands().get(1), blackjackPlayer.getActiveHand());
	}

	@Test
	public void takingCardOnSplittedHandAfterCallingStandAddsUpToSecondHand() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(11, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.SPADE));
		table.trySeat(5, blackjackPlayer);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.splitStartingHand(blackjackPlayer);
		table.takeCard(blackjackPlayer);
		table.stand(blackjackPlayer);
		table.takeCard(blackjackPlayer);
		assertEquals(2, blackjackPlayer.getActiveHand().getCards().size());
		assertEquals(13, blackjackPlayer.getActiveHand().calculateValues().get(0));
	}

	@Test
	public void callingStandOnSecondHandOfSplitCompletesSecondHand() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(11, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.DIAMOND));
		cards.add(Card.of(3, Suit.SPADE));
		table.trySeat(5, blackjackPlayer);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.splitStartingHand(blackjackPlayer);
		table.takeCard(blackjackPlayer);
		table.stand(blackjackPlayer);
		table.takeCard(blackjackPlayer);
		table.stand(blackjackPlayer);
		assertNull(blackjackPlayer.getActiveHand());
		assertTrue(blackjackPlayer.getHands().get(1).isCompleted());
	}

	@Test
	public void pictureCardsCanBeSplitted() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(11, Suit.DIAMOND));
		cards.add(Card.of(12, Suit.SPADE));
		table.trySeat(5, blackjackPlayer);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.splitStartingHand(blackjackPlayer);
		assertEquals(19, blackjackPlayer.getActiveHand().calculateValues().get(0));
		assertEquals(10, blackjackPlayer.getHands().get(1).calculateValues().get(0));
	}

	@Test
	public void splittingAcesCreatesTwoValues() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(10, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.SPADE));
		table.trySeat(5, blackjackPlayer);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.splitStartingHand(blackjackPlayer);
		List<Integer> activeHandValues = blackjackPlayer.getActiveHand().calculateValues();
		assertEquals(2, activeHandValues.size());
		assertEquals(2, blackjackPlayer.getHands().get(1).calculateValues().size());
		assertEquals(10, activeHandValues.get(0));
		assertEquals(20, activeHandValues.get(1));
		assertEquals(1, blackjackPlayer.getHands().get(1).calculateValues().get(0));
		assertEquals(11, blackjackPlayer.getHands().get(1).calculateValues().get(1));
	}
}