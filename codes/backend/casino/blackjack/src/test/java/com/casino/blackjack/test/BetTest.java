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

import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.blackjack.rules.BlackjackDealer;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.bet.BetUtil;
import com.casino.common.cards.Card;
import com.casino.common.cards.Suit;
import com.casino.common.exception.IllegalBetException;
import com.casino.common.exception.IllegalPlayerActionException;
import com.casino.common.exception.PlayerNotFoundException;
import com.casino.common.table.Status;
import com.casino.common.table.Thresholds;
import com.casino.common.table.Type;
import com.casino.common.table.phase.GamePhase;
import com.casino.common.user.Bridge;

public class BetTest extends BaseTest {
	private BlackjackTable table;
	private BlackjackDealer dealer;

//	private BlackjackPlayer blackjackPlayer;

	@BeforeEach
	public void initTest() {
		try {
			table = new BlackjackTable(Status.WAITING_PLAYERS,
					new Thresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS, MIN_PLAYERS, MAX_PLAYERS, DEFAULT_SEAT_COUNT, Type.PUBLIC),
					UUID.randomUUID());
			bridge = new Bridge("JohnDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000"));
			bridge2 = new Bridge("JaneDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000"));
			Field f = table.getClass().getDeclaredField("dealer");
//			blackjackPlayer = new BlackjackPlayer(bridge, table);
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
		table.join(bridge, 0);
		sleep(BET_ROUND_TIME_SECONDS + 1, ChronoUnit.SECONDS);
		IllegalPlayerActionException exception = assertThrows(IllegalPlayerActionException.class, () -> {
			table.bet(bridge.tableId(), new BigDecimal("50.0"));
		});
		assertEquals(16, exception.getCode());
	}

	@Test
	public void betUtilChecksCorrectPhase() {
		table.updateGamePhase(GamePhase.BETS_COMPLETED);
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer(bridge, table);
		IllegalBetException exception = assertThrows(IllegalBetException.class, () -> {
			BetUtil.verifyStartingBet(table, blackjackPlayer, MAX_BET);
		});
		assertEquals(1, exception.getCode());
	}

	@Test
	public void emptyBetResultsToException() {
		table.join(bridge, 0);
		IllegalBetException exception = assertThrows(IllegalBetException.class, () -> {
			table.bet(bridge.playerId(), null);
		});
		assertEquals(2, exception.getCode());
	}

	@Test
	public void placingBetOverBalanceButWithinTableLimitsResultsToException() {
		table.join(bridge, 0);
		IllegalBetException exception = assertThrows(IllegalBetException.class, () -> {
			table.bet(bridge.playerId(), new BigDecimal("1000.001"));
		});
		assertEquals(3, exception.getCode());
	}

	@Test
	public void placingBetToPlayerNotInTableResultsInException() {
		table.join(bridge, 0);
		PlayerNotFoundException exception = assertThrows(PlayerNotFoundException.class, () -> {
			table.bet(bridge2.playerId(), new BigDecimal("7.0"));
		});
		assertEquals(1, exception.getCode());
	}

	@Test
	public void placingBetUnderTableMinimumResultsException() {
		table.join(bridge, 0);
		IllegalBetException exception = assertThrows(IllegalBetException.class, () -> {
			table.bet(bridge.playerId(), new BigDecimal("4.99"));
		});
		assertEquals(4, exception.getCode());
	}

	@Test
	public void placingBetOverTableMaximuResultsToException() {
		table.join(bridge, 0);
		IllegalBetException exception = assertThrows(IllegalBetException.class, () -> {
			table.bet(bridge.playerId(), new BigDecimal("100.1"));
		});
		assertEquals(5, exception.getCode());
	}

	@Test
	public void placingAllowedBetSetsTheBetForPlayer() {
		table.join(bridge, 0);
		table.bet(bridge.playerId(), new BigDecimal("49.9"));
		assertEquals("49.90", table.getPlayer(bridge.playerId()).getTotalBet().toString());
	}

	@Test
	public void playersBetsAreAccepted() {
		assertTrue(table.join(bridge, 1));
		assertTrue(table.join(bridge2, 2));
		table.bet(bridge.playerId(), new BigDecimal("50.0"));
		table.bet(bridge2.playerId(), new BigDecimal("99.7"));
		assertEquals("50.00", table.getPlayer(bridge.playerId()).getTotalBet().toString());
		assertEquals("99.70", table.getPlayer(bridge2.playerId()).getTotalBet().toString());
	}

	@Test
	public void creatingATableWithNegativeBetAmountIsNotAllowed() { // In error case where minimum bet is negative
		assertThrows(IllegalArgumentException.class, () -> {
			new BlackjackTable(Status.WAITING_PLAYERS, new Thresholds(new BigDecimal("-1000.0"), MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS, MIN_PLAYERS,
					MAX_PLAYERS, DEFAULT_SEAT_COUNT, Type.PUBLIC), UUID.randomUUID());
		});
	}

	@Test
	public void tableCannotBeCreatedIfMaximumBetIsLessThanMinimumBet() { // In error case where minimum bet is negative
		assertThrows(IllegalArgumentException.class, () -> {
			new BlackjackTable(Status.WAITING_PLAYERS, new Thresholds(new BigDecimal("1000.0"), new BigDecimal("100.0"), BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS,
					MIN_PLAYERS, MAX_PLAYERS, DEFAULT_SEAT_COUNT, Type.PUBLIC), UUID.randomUUID());

		});
	}

	@Test
	public void negativeStartingBetIsPrevented() { // In error case where minimum bet is negative
		BlackjackTable table = new BlackjackTable(Status.WAITING_PLAYERS,
				new Thresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS, MIN_PLAYERS, MAX_PLAYERS, DEFAULT_SEAT_COUNT, Type.PUBLIC),
				UUID.randomUUID());
		bridge = new Bridge("JohnDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000"));
		table.join(bridge, 1);
		assertThrows(IllegalBetException.class, () -> {
			table.bet(bridge.playerId(), new BigDecimal("-10.1"));
		});
	}
}
