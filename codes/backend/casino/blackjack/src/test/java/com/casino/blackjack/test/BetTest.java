package com.casino.blackjack.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.bet.BetThresholds;
import com.casino.common.exception.IllegalBetException;
import com.casino.common.exception.PlayerNotFoundException;
import com.casino.common.table.PlayerRange;
import com.casino.common.table.Status;
import com.casino.common.table.Type;

public class BetTest extends BaseTest {
	@Test
	public void placingBetWhenBetPhaseIsCompleteResultsToException() {
		BlackjackTable table = new BlackjackTable(Status.WAITING_PLAYERS, new BetThresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, PLAYER_TIME, INITIAL_DELAY), new PlayerRange(1, 6), Type.PUBLIC, 15, UUID.randomUUID());
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		table.trySeat(0, blackjackPlayer);
		sleep(BET_ROUND_TIME_SECONDS + 1, ChronoUnit.SECONDS);
		IllegalBetException exception = assertThrows(IllegalBetException.class, () -> {
			table.placeStartingBet(blackjackPlayer, new BigDecimal("50.0"));
		});
		assertEquals(1, exception.getCode());
	}

	@Test
	public void emptyBetResultsToException() {
		BlackjackTable table = new BlackjackTable(Status.WAITING_PLAYERS, new BetThresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, PLAYER_TIME, INITIAL_DELAY), new PlayerRange(1, 6), Type.PUBLIC, 15, UUID.randomUUID());
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		table.trySeat(0, blackjackPlayer);
		IllegalBetException exception = assertThrows(IllegalBetException.class, () -> {
			table.placeStartingBet(blackjackPlayer, null);
		});
		assertEquals(2, exception.getCode());
	}

	@Test
	public void placingBetOverBalanceButWithinTableLimitsResultsToException() {
		BlackjackTable table = new BlackjackTable(Status.WAITING_PLAYERS, new BetThresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, PLAYER_TIME, INITIAL_DELAY), new PlayerRange(1, 6), Type.PUBLIC, 15, UUID.randomUUID());
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("50.0"), table);
		table.trySeat(0, blackjackPlayer);
		IllegalBetException exception = assertThrows(IllegalBetException.class, () -> {
			table.placeStartingBet(blackjackPlayer, new BigDecimal("50.001"));
		});
		assertEquals(3, exception.getCode());
	}

	@Test
	public void placingBetToPlayerNotInTableResultsInException() {
		BlackjackTable table = new BlackjackTable(Status.WAITING_PLAYERS, new BetThresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, PLAYER_TIME, INITIAL_DELAY), new PlayerRange(1, 6), Type.PUBLIC, 15, UUID.randomUUID());
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
		BlackjackTable table = new BlackjackTable(Status.WAITING_PLAYERS, new BetThresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, PLAYER_TIME, INITIAL_DELAY), new PlayerRange(1, 6), Type.PUBLIC, 15, UUID.randomUUID());
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		table.trySeat(0, blackjackPlayer);
		IllegalBetException exception = assertThrows(IllegalBetException.class, () -> {
			table.placeStartingBet(blackjackPlayer, new BigDecimal("4.99"));
		});
		assertEquals(4, exception.getCode());
	}

	@Test
	public void placingBetOverTableMaximuResultsToException() {
		BlackjackTable table = new BlackjackTable(Status.WAITING_PLAYERS, new BetThresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, PLAYER_TIME, INITIAL_DELAY), new PlayerRange(1, 6), Type.PUBLIC, 15, UUID.randomUUID());
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe!!", UUID.randomUUID(), new BigDecimal("1000"), table);
		table.trySeat(0, blackjackPlayer);
		IllegalBetException exception = assertThrows(IllegalBetException.class, () -> {
			table.placeStartingBet(blackjackPlayer, new BigDecimal("100.1"));
		});
		assertEquals(5, exception.getCode());
	}

	@Test
	public void placingAllowedBetSetsTheBetForPlayer() {
		BlackjackTable table = new BlackjackTable(Status.WAITING_PLAYERS, new BetThresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, PLAYER_TIME, INITIAL_DELAY), new PlayerRange(1, 6), Type.PUBLIC, 15, UUID.randomUUID());
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("50"), table);
		table.trySeat(0, blackjackPlayer);
		table.placeStartingBet(blackjackPlayer, new BigDecimal("49.9"));
		assertEquals(blackjackPlayer.getBet().toString(), "49.9");
	}

	@Test
	public void playersBetsAreAccepted() {
		BlackjackTable table = new BlackjackTable(Status.WAITING_PLAYERS, new BetThresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, PLAYER_TIME, INITIAL_DELAY), new PlayerRange(1, 7), Type.PUBLIC, 7, UUID.randomUUID());
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		BlackjackPlayer blackjackPlayer2 = new BlackjackPlayer("JaneDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		assertTrue(table.trySeat(1, blackjackPlayer));
		assertTrue(table.trySeat(2, blackjackPlayer2));
		// var dealer = (BlackjackDealer) table.getDealer();
		table.placeStartingBet(blackjackPlayer, new BigDecimal("50.0"));
		table.placeStartingBet(blackjackPlayer2, new BigDecimal("99.7"));
		assertEquals("50.0", blackjackPlayer.getBet().toString());
		assertEquals("99.7", blackjackPlayer2.getBet().toString());
	}
}
