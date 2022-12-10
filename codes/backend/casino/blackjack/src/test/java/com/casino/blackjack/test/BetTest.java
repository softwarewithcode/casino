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

public class BetTest extends BaseTest {
	private BlackjackTable table;
	private BlackjackDealer dealer;

	@BeforeEach
	public void initTest() {
		try {
			table = new BlackjackTable(Status.WAITING_PLAYERS, new Thresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS, MIN_PLAYERS, MAX_PLAYERS, DEFAULT_SEAT_COUNT, Type.PUBLIC),
					UUID.randomUUID());
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
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		table.trySeat(0, blackjackPlayer);
		sleep(BET_ROUND_TIME_SECONDS + 1, ChronoUnit.SECONDS);
		IllegalPlayerActionException exception = assertThrows(IllegalPlayerActionException.class, () -> {
			table.placeStartingBet(blackjackPlayer, new BigDecimal("50.0"));
		});
		assertEquals(16, exception.getCode());
	}

	@Test
	public void betUtilChecksCorrectPhase() {
		table.updateGamePhase(GamePhase.BETS_COMPLETED);
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		IllegalBetException exception = assertThrows(IllegalBetException.class, () -> {
			BetUtil.verifyStartingBet(table, blackjackPlayer, MAX_BET);
		});
		assertEquals(1, exception.getCode());
	}

	@Test
	public void emptyBetResultsToException() {
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		table.trySeat(0, blackjackPlayer);
		IllegalBetException exception = assertThrows(IllegalBetException.class, () -> {
			table.placeStartingBet(blackjackPlayer, null);
		});
		assertEquals(2, exception.getCode());
	}

	@Test
	public void placingBetOverBalanceButWithinTableLimitsResultsToException() {
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("50.0"), table);
		table.trySeat(0, blackjackPlayer);
		IllegalBetException exception = assertThrows(IllegalBetException.class, () -> {
			table.placeStartingBet(blackjackPlayer, new BigDecimal("50.001"));
		});
		assertEquals(3, exception.getCode());
	}

	@Test
	public void placingBetToPlayerNotInTableResultsInException() {
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		BlackjackPlayer blackjackPlayer2 = new BlackjackPlayer("JaneDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		table.trySeat(0, blackjackPlayer);
		PlayerNotFoundException exception = assertThrows(PlayerNotFoundException.class, () -> {
			table.placeStartingBet(blackjackPlayer2, new BigDecimal("7.0"));
		});
		assertEquals(1, exception.getCode());
	}

	@Test
	public void placingBetUnderTableMinimumResultsException() {
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		table.trySeat(0, blackjackPlayer);
		IllegalBetException exception = assertThrows(IllegalBetException.class, () -> {
			table.placeStartingBet(blackjackPlayer, new BigDecimal("4.99"));
		});
		assertEquals(4, exception.getCode());
	}

	@Test
	public void placingBetOverTableMaximuResultsToException() {
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe!!", UUID.randomUUID(), new BigDecimal("1000"), table);
		table.trySeat(0, blackjackPlayer);
		IllegalBetException exception = assertThrows(IllegalBetException.class, () -> {
			table.placeStartingBet(blackjackPlayer, new BigDecimal("100.1"));
		});
		assertEquals(5, exception.getCode());
	}

	@Test
	public void placingAllowedBetSetsTheBetForPlayer() {
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("50"), table);
		table.trySeat(0, blackjackPlayer);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("49.9"));
		assertEquals("49.90", blackjackPlayer.getTotalBet().toString());
	}

	@Test
	public void playersBetsAreAccepted() {
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		BlackjackPlayer blackjackPlayer2 = new BlackjackPlayer("JaneDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		assertTrue(table.trySeat(1, blackjackPlayer));
		assertTrue(table.trySeat(2, blackjackPlayer2));
		table.placeStartingBet(blackjackPlayer, new BigDecimal("50.0"));
		table.placeStartingBet(blackjackPlayer2, new BigDecimal("99.7"));
		assertEquals("50.00", blackjackPlayer.getTotalBet().toString());
		assertEquals("99.70", blackjackPlayer2.getTotalBet().toString());
	}

	@Test
	public void negativeStartingBetIsPreventeted() { // In error case where minimum bet is negative
		BlackjackTable table = new BlackjackTable(Status.WAITING_PLAYERS,
				new Thresholds(new BigDecimal("-1000.0"), MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS, MIN_PLAYERS, MAX_PLAYERS, DEFAULT_SEAT_COUNT, Type.PUBLIC), UUID.randomUUID());
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		table.trySeat(0, blackjackPlayer);
		IllegalBetException exception = assertThrows(IllegalBetException.class, () -> {
			table.placeStartingBet(blackjackPlayer, new BigDecimal("-10.1"));
		});
		assertEquals(9, exception.getCode());
	}
}
