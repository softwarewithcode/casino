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

import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.blackjack.rules.BlackjackDealer;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.cards.Card;
import com.casino.common.cards.Suit;
import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.table.ISeatedTable;
import com.casino.common.table.Status;
import com.casino.common.table.Thresholds;
import com.casino.common.table.Type;

public class StartingHandDoubleTest extends BaseTest {
//	protected static final BigDecimal MIN_BET = new BigDecimal("5.0");
//	protected static final BigDecimal MAX_BET = new BigDecimal("100.0");
//	protected static final Integer PLAYER_TIME = 10;
//	protected static final Integer INITIAL_DELAY = 0;
	protected ISeatedTable publicTable;
	protected ICasinoPlayer blackjackPlayer2;
	private BlackjackTable table;
	private BlackjackPlayer blackjackPlayer;
	private BlackjackDealer dealer;
	private BigDecimal initialBet = new BigDecimal("25.78");

	@BeforeEach
	public void initTest() {
		try {
			table = new BlackjackTable(Status.WAITING_PLAYERS, new Thresholds(new BigDecimal("0.001"), MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS,
					MIN_PLAYERS, MAX_PLAYERS, DEFAULT_SEAT_COUNT, Type.PUBLIC), UUID.randomUUID());
			blackjackPlayer = new BlackjackPlayer("JohnDoe2", UUID.randomUUID(), new BigDecimal("1000"), publicTable);
			blackjackPlayer2 = new BlackjackPlayer("JaneDoe2", UUID.randomUUID(), new BigDecimal("1000"), publicTable);
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
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), initialBet);
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.doubleDown(blackjackPlayer.getId());
		assertEquals(new BigDecimal("51.56"), table.getPlayer(blackjackPlayer.getId()).getTotalBet());
		assertEquals(new BigDecimal("51.56"), table.getPlayer(blackjackPlayer.getId()).getHands().get(0).getBet());
	}

	@Test
	public void doubledHandGetsCompleted() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(4, Suit.SPADE));
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), initialBet);
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.doubleDown(blackjackPlayer.getId());
		assertTrue(table.getPlayer(blackjackPlayer.getId()).getHands().get(0).isCompleted());
		assertNull(table.getPlayer(blackjackPlayer.getId()).getActiveHand());
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.hit(blackjackPlayer.getId());
		});
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.stand(blackjackPlayer.getId());
		});
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.doubleDown(blackjackPlayer.getId());
		});
	}

	@Test
	public void doubledHandGetsOneAdditionalCard() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(1, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.HEART));
		cards.add(Card.of(4, Suit.SPADE));
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), initialBet);
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.doubleDown(blackjackPlayer.getId());
		assertTrue(table.getPlayer(blackjackPlayer.getId()).getHands().get(0).isCompleted());
		assertEquals(10, table.getPlayer(blackjackPlayer.getId()).getHands().get(0).calculateValues().get(0));
		assertEquals(20, table.getPlayer(blackjackPlayer.getId()).getHands().get(0).calculateValues().get(1));
		assertEquals(3, table.getPlayer(blackjackPlayer.getId()).getHands().get(0).getCards().size());
		assertNull(table.getPlayer(blackjackPlayer.getId()).getActiveHand());
	}

	@Test
	public void doublingTenResultsToDoubleBet() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(5, Suit.SPADE));
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), initialBet);
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.doubleDown(blackjackPlayer.getId());
		assertEquals(new BigDecimal("51.56"), table.getPlayer(blackjackPlayer.getId()).getTotalBet());
	}

	@Test
	public void doublingElevenResultsToDoubleBet() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(2, Suit.DIAMOND));
		cards.add(Card.of(6, Suit.SPADE));
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), initialBet);
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		table.doubleDown(blackjackPlayer.getId());
		assertEquals(new BigDecimal("51.56"), table.getPlayer(blackjackPlayer.getId()).getTotalBet());
		assertEquals(new BigDecimal("51.56"), table.getPlayer(blackjackPlayer.getId()).getHands().get(0).getBet());
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
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("10.0"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.doubleDown(blackjackPlayer.getId());
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
		blackjackPlayer = new BlackjackPlayer("JohnDoe2", UUID.randomUUID(), new BigDecimal("199.99"), publicTable);
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("100"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertThrows(IllegalArgumentException.class, () -> {
			table.doubleDown(blackjackPlayer.getId());
		});
	}

	@Test
	public void doublingIsAllowedOnlyOnce() {
		List<Card> cards = dealer.getDecks();
		cards.add(Card.of(5, Suit.DIAMOND));
		cards.add(Card.of(9, Suit.DIAMOND));
		cards.add(Card.of(6, Suit.SPADE));
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 5);
		table.bet(blackjackPlayer.getId(), new BigDecimal("0.01234"));
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertEquals(new BigDecimal("999.98"), table.getPlayer(blackjackPlayer.getId()).getBalance());
		table.doubleDown(blackjackPlayer.getId());
		assertEquals(new BigDecimal("0.02"), table.getPlayer(blackjackPlayer.getId()).getTotalBet());
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.doubleDown(blackjackPlayer.getId());
		});
	}
}
