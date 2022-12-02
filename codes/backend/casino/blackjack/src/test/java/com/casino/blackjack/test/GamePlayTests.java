package com.casino.blackjack.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.blackjack.rules.BlackjackDealer;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.bet.BetThresholds;
import com.casino.common.cards.Card;
import com.casino.common.cards.Suit;
import com.casino.common.table.PlayerRange;
import com.casino.common.table.Status;
import com.casino.common.table.Type;

public class GamePlayTests extends BaseTest {

	@Test
	public void playerCannotTakeCardsAfterBusted21() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		BlackjackTable table = new BlackjackTable(Status.WAITING_PLAYERS, new BetThresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, PLAYER_TIME, INITIAL_DELAY), new PlayerRange(1, 7), Type.PUBLIC, 7, UUID.randomUUID());
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), publicTable);
		// BlackjackDealer dealer = new BlackjackDealer(table, null);
		Field f = table.getClass().getDeclaredField("dealer");
		f.setAccessible(true);
		BlackjackDealer dealer = (BlackjackDealer) f.get(table);
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(4, Suit.CLUB));
		cards.add(Card.of(8, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.SPADE));
		table.trySeat(5, blackjackPlayer);
		table.placeInitialBet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		// dealer.dealInitialCards();
		assertEquals(5, blackjackPlayer.getHands().get(0).getCards().get(0).getRank());
		assertEquals(Suit.SPADE, blackjackPlayer.getHands().get(0).getCards().get(0).getSuit());
		assertEquals(9, blackjackPlayer.getHands().get(0).getCards().get(1).getRank());
		assertEquals(Suit.DIAMOND, blackjackPlayer.getHands().get(0).getCards().get(1).getSuit());
		assertTrue(blackjackPlayer.canTake());
		table.takeCard(blackjackPlayer);
		assertEquals(3, blackjackPlayer.getHands().get(0).getCards().size());
		assertEquals(8, blackjackPlayer.getHands().get(0).getCards().get(2).getRank());
		assertEquals(Suit.DIAMOND, blackjackPlayer.getHands().get(0).getCards().get(2).getSuit());
		assertEquals(22, blackjackPlayer.getHands().get(0).calculateValues().get(0));
		assertFalse(blackjackPlayer.canTake());
	}

	@Test
	public void playerCannotTakeCardsWhenHandValueIs21() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		BlackjackTable table = new BlackjackTable(Status.WAITING_PLAYERS, new BetThresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, PLAYER_TIME, INITIAL_DELAY), new PlayerRange(1, 7), Type.PUBLIC, 7, UUID.randomUUID());
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), publicTable);
		// BlackjackDealer dealer = new BlackjackDealer(table, null);
		Field f = table.getClass().getDeclaredField("dealer");
		f.setAccessible(true);
		BlackjackDealer dealer = (BlackjackDealer) f.get(table);
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(4, Suit.CLUB));
		cards.add(Card.of(7, Suit.HEART));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.SPADE));
		table.trySeat(5, blackjackPlayer);
		table.placeInitialBet(blackjackPlayer, new BigDecimal("99.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		// dealer.dealInitialCards();
		assertEquals(5, blackjackPlayer.getHands().get(0).getCards().get(0).getRank());
		assertEquals(Suit.SPADE, blackjackPlayer.getHands().get(0).getCards().get(0).getSuit());
		assertEquals(9, blackjackPlayer.getHands().get(0).getCards().get(1).getRank());
		assertEquals(Suit.DIAMOND, blackjackPlayer.getHands().get(0).getCards().get(1).getSuit());
		assertTrue(blackjackPlayer.canTake());
		table.takeCard(blackjackPlayer);
		assertEquals(3, blackjackPlayer.getHands().get(0).getCards().size());
		assertEquals(7, blackjackPlayer.getHands().get(0).getCards().get(2).getRank());
		assertEquals(Suit.HEART, blackjackPlayer.getHands().get(0).getCards().get(2).getSuit());
		assertFalse(blackjackPlayer.canTake());
		assertEquals(21, blackjackPlayer.getHands().get(0).calculateValues().get(0));
	}
}
