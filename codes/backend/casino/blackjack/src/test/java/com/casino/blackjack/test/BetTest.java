package com.casino.blackjack.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.casino.blackjack.game.BlackjackGamePhase;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.cards.Card;
import com.casino.common.cards.Suit;
import com.casino.common.exception.IllegalBetException;
import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.common.exception.PlayerNotFoundException;
import com.casino.common.user.Bridge;

public class BetTest extends BaseTest {
	private BlackjackTable table;
	private BlackjackDealer dealer;

	@BeforeEach
	public void initTest() {
		try {
			table = new BlackjackTable(getDefaultTableInitData(), blackjackInitData);
			bridge = new Bridge("JohnDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000"));
			bridge2 = new Bridge("JaneDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000"));
			Field f = table.getClass().getDeclaredField("dealer");
			f.setAccessible(true);
			dealer = (BlackjackDealer) f.get(table);
			List<Card> cards = dealer.getDecks();
			cards.add(Card.of(4, Suit.CLUB));
			cards.add(Card.of(8, Suit.DIAMOND));
			cards.add(Card.of(9, Suit.DIAMOND));
			cards.add(Card.of(9, Suit.HEART));
			cards.add(Card.of(5, Suit.SPADE));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void placingBetWhenBetPhaseIsCompleteResultsToException() {
		table.join(bridge, "0");
		sleep(BET_ROUND_TIME_SECONDS + 1, ChronoUnit.SECONDS);
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.bet(bridge.tableId(), new BigDecimal("50.0"));
		});
	}

	@Test
	public void betIsNotAcceptedIfGamePhaseIsNotBet() {
		table.updateGamePhase(BlackjackGamePhase.BETS_COMPLETED);
		table.join(bridge, "0");
		sleep(BET_ROUND_TIME_SECONDS, ChronoUnit.SECONDS);
		assertThrows(IllegalPlayerActionException.class, () -> {
			table.bet(bridge.userId(), new BigDecimal("15"));
		});
	}

	@Test
	public void emptyBetResultsToException() {
		table.join(bridge, "0");
		IllegalBetException exception = assertThrows(IllegalBetException.class, () -> {
			table.bet(bridge.userId(), null);
		});
		assertEquals(2, exception.getCode());
	}

	@Test
	public void placingBetOverBalanceButWithinTableLimitsResultsToException() {
		table.join(bridge, "0");
		IllegalBetException exception = assertThrows(IllegalBetException.class, () -> {
			table.bet(bridge.userId(), new BigDecimal("1000.001"));
		});
		assertEquals(3, exception.getCode());
	}

	@Test
	public void placingBetToPlayerNotInTableResultsInException() {
		table.join(bridge, "0");
		assertThrows(PlayerNotFoundException.class, () -> {
			table.bet(bridge2.userId(), new BigDecimal("7.0"));
		});
	}

	@Test
	public void placingBetUnderTableMinimumResultsException() {
		table.join(bridge, "0");
		IllegalBetException exception = assertThrows(IllegalBetException.class, () -> {
			table.bet(bridge.userId(), new BigDecimal("4.99"));
		});
		assertEquals(4, exception.getCode());
	}

	@Test
	public void placingBetOverTableMaximuResultsToException() {
		table.join(bridge, "0");
		IllegalBetException exception = assertThrows(IllegalBetException.class, () -> {
			table.bet(bridge.userId(), new BigDecimal("100.1"));
		});
		assertEquals(5, exception.getCode());
	}

	@Test
	public void placingAllowedBetSetsTheBetForPlayer() {
		table.join(bridge, "0");
		table.bet(bridge.userId(), new BigDecimal("49.9"));
		assertEquals("49.90", table.getPlayer(bridge.userId()).getTotalBet().toString());
	}

	@Test
	public void playersBetsAreAccepted() {
		assertTrue(table.join(bridge, "1"));
		assertTrue(table.join(bridge2, "2"));
		table.bet(bridge.userId(), new BigDecimal("50.0"));
		table.bet(bridge2.userId(), new BigDecimal("99.7"));
		assertEquals("50.00", table.getPlayer(bridge.userId()).getTotalBet().toString());
		assertEquals("99.70", table.getPlayer(bridge2.userId()).getTotalBet().toString());
	}

	@Test
	public void creatingTableWithNegativeMinBetAmountIsNotAllowed() { // In error case where minimum bet is negative
		assertThrows(IllegalArgumentException.class, () -> {
			createBlackjackInitData(MIN_BUYIN, new BigDecimal("-1000.0"), MAX_BET, 5, 5, 5, 5, null);
		});
	}

	@Test
	public void tableCannotBeCreatedIfMaximumBetIsLessThanMinimumBet() {
		assertThrows(IllegalArgumentException.class, () -> {
			createBlackjackInitData(MIN_BUYIN, new BigDecimal("2.0"), MAX_BET, 5, 5, 5, 5, null);
		});
	}

	@Test
	public void negativeStartingBetIsPrevented() {
		table = new BlackjackTable(getDefaultTableInitData(), blackjackInitData);
		table.join(bridge, "1");
		assertThrows(IllegalBetException.class, () -> {
			table.bet(bridge.userId(), new BigDecimal("-10.1"));
		});
	}
}
