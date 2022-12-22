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
import com.casino.common.table.Status;
import com.casino.common.table.TableInitData;
import com.casino.common.table.Thresholds;
import com.casino.common.user.Bridge;

public class StartingHandDoubleTest extends BaseTest {
	private BlackjackTable table;
	private BlackjackDealer dealer;
	private BigDecimal initialBet = new BigDecimal("25.78");

	@BeforeEach
	public void initTest() {
		try {
			Thresholds thresholds = new Thresholds(new BigDecimal("0.001"), MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS, MIN_PLAYERS, MAX_PLAYERS,
					DEFAULT_SEAT_COUNT);
			TableInitData tableInitData = getDefaultTableInitDataWithThresholds(thresholds);
			table = new BlackjackTable(Status.WAITING_PLAYERS, tableInitData);
			bridge = new Bridge("JohnDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
			bridge2 = new Bridge("JaneDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
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
		table.join(bridge, "5");
		table.bet(bridge.userId(), initialBet);
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.doubleDown(bridge.userId());
		assertEquals(new BigDecimal("51.56"), table.getPlayer(bridge.userId()).getTotalBet());
		assertEquals(new BigDecimal("51.56"), table.getPlayer(bridge.userId()).getHands().get(0).getBet());
	}

	@Test
	public void doubledHandGetsCompleted() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(4, Suit.SPADE));
		table.join(bridge, "5");
		table.bet(bridge.userId(), initialBet);
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.doubleDown(bridge.userId());
		assertTrue(table.getPlayer(bridge.userId()).getHands().get(0).isCompleted());
		assertNull(table.getPlayer(bridge.userId()).getActiveHand());
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.hit(bridge.userId());
		});
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.stand(bridge.userId());
		});
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.doubleDown(bridge.userId());
		});
	}

	@Test
	public void doubledHandGetsOneAdditionalCard() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.HEART));
		cards.add(Card.of(4, Suit.SPADE));
		table.join(bridge, "5");
		table.bet(bridge.userId(), initialBet);
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.doubleDown(bridge.userId());
		assertTrue(table.getPlayer(bridge.userId()).getHands().get(0).isCompleted());
		assertEquals(10, table.getPlayer(bridge.userId()).getHands().get(0).calculateValues().get(0));
		assertEquals(20, table.getPlayer(bridge.userId()).getHands().get(0).calculateValues().get(1));
		assertEquals(3, table.getPlayer(bridge.userId()).getHands().get(0).getCards().size());
		assertNull(table.getPlayer(bridge.userId()).getActiveHand());
	}

	@Test
	public void doublingTenResultsToDoubleBet() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.SPADE));
		table.join(bridge, "5");
		table.bet(bridge.userId(), initialBet);
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.doubleDown(bridge.userId());
		assertEquals(new BigDecimal("51.56"), table.getPlayer(bridge.userId()).getTotalBet());
	}

	@Test
	public void doublingElevenResultsToDoubleBet() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(6, Suit.SPADE));
		table.join(bridge, "5");
		table.bet(bridge.userId(), initialBet);
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.doubleDown(bridge.userId());
		assertEquals(new BigDecimal("51.56"), table.getPlayer(bridge.userId()).getTotalBet());
		assertEquals(new BigDecimal("51.56"), table.getPlayer(bridge.userId()).getHands().get(0).getBet());
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
		table.join(bridge, "5");
		table.bet(bridge.userId(), new BigDecimal("10.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.doubleDown(bridge.userId());
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
		bridge = new Bridge("JohnDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("199.9"));
		table.join(bridge, "5");
		table.bet(bridge.userId(), new BigDecimal("100"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertThrows(IllegalArgumentException.class, () -> {
			table.doubleDown(bridge.userId());
		});
	}

	@Test
	public void doublingIsAllowedOnlyOnce() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(6, Suit.SPADE));
		table.join(bridge, "5");
		table.bet(bridge.userId(), new BigDecimal("0.01234"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("999.98"), table.getPlayer(bridge.userId()).getBalance());
		table.doubleDown(bridge.userId());
		assertEquals(new BigDecimal("0.02"), table.getPlayer(bridge.userId()).getTotalBet());
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.doubleDown(bridge.userId());
		});
	}
}
