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
import com.casino.common.table.TableData;
import com.casino.common.table.TableThresholds;
import com.casino.common.user.User;

public class StartingHandDoubleTest extends BaseTest {
	private BlackjackTable table;
	private BlackjackDealer dealer;
	private BigDecimal initialBet = new BigDecimal("25.78");

	@BeforeEach
	public void initTest() {
		try {
			TableThresholds thresholds = new TableThresholds(MIN_PLAYERS, MAX_PLAYERS, DEFAULT_SEAT_COUNT);
			TableData tableInitData = getDefaultTableInitDataWithThresholds(thresholds);
			table = new BlackjackTable(tableInitData,blackjackInitData);
			user = new User("JohnDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
			user2 = new User("JaneDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
			Field f = table.getClass().getDeclaredField("dealer");
			f.setAccessible(true);
			dealer = (BlackjackDealer) f.get(table);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void doublingNineResultsToDoubleBet() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(4, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), initialBet);
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.doubleDown(user.userId());
		assertEquals(new BigDecimal("51.56"), table.getPlayer(user.userId()).getTotalBet());
		assertEquals(new BigDecimal("51.56"), table.getPlayer(user.userId()).getHands().get(0).getBet());
	}

	@Test
	public void doubledHandGetsCompleted() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(4, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), initialBet);
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.doubleDown(user.userId());
		assertTrue(table.getPlayer(user.userId()).getHands().get(0).isCompleted());
		assertNull(table.getPlayer(user.userId()).getActiveHand());
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.hit(user.userId());
		});
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.stand(user.userId());
		});
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.doubleDown(user.userId());
		});
	}

	@Test
	public void doubledHandGetsOneAdditionalCard() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.HEART));
		cards.add(Card.of(4, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), initialBet);
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.doubleDown(user.userId());
		assertTrue(table.getPlayer(user.userId()).getHands().get(0).isCompleted());
		assertEquals(10, table.getPlayer(user.userId()).getHands().get(0).calculateValues().get(0));
		assertEquals(20, table.getPlayer(user.userId()).getHands().get(0).calculateValues().get(1));
		assertEquals(3, table.getPlayer(user.userId()).getHands().get(0).getCards().size());
		assertNull(table.getPlayer(user.userId()).getActiveHand());
	}

	@Test
	public void doublingTenResultsToDoubleBet() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), initialBet);
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.doubleDown(user.userId());
		assertEquals(new BigDecimal("51.56"), table.getPlayer(user.userId()).getTotalBet());
	}

	@Test
	public void doublingElevenResultsToDoubleBet() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(6, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), initialBet);
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.doubleDown(user.userId());
		assertEquals(new BigDecimal("51.56"), table.getPlayer(user.userId()).getTotalBet());
		assertEquals(new BigDecimal("51.56"), table.getPlayer(user.userId()).getHands().get(0).getBet());
	}

	@Test
	public void doublingBlackjackIsPrevented() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(6, Suit.DIAMOND));
		cards.add(Card.of(6, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(12, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.SPADE));
		cards.add(Card.of(1, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("10.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.doubleDown(user.userId());
		});
	}

	@Test
	public void doublingAllowedOnlyIfEnoughBalance() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(6, Suit.DIAMOND));
		cards.add(Card.of(6, Suit.DIAMOND));
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(8, Suit.DIAMOND));
		cards.add(Card.of(10, Suit.SPADE));
		cards.add(Card.of(2, Suit.SPADE));
		user = new User("JohnDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("199.9"));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("100"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.doubleDown(user.userId());
		});
	}

	@Test
	public void doublingIsAllowedOnlyOnce() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(6, Suit.SPADE));
		table.join(user, "5");
		table.bet(user.userId(), new BigDecimal("5.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("995.00"), table.getPlayer(user.userId()).getCurrentBalance());
		table.doubleDown(user.userId());
		assertEquals(new BigDecimal("10.00"), table.getPlayer(user.userId()).getTotalBet());
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.doubleDown(user.userId());
		});
	}
}
