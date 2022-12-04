package com.casino.blackjack.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.bet.BetThresholds;
import com.casino.common.table.PlayerRange;
import com.casino.common.table.Status;
import com.casino.common.table.Type;

public class SeatTest extends BaseTest {

	@Test
	public void initialTableJoinSetsPlayerAsWatcherInPublicTable() {
		BlackjackTable table = new BlackjackTable(Status.WAITING_PLAYERS, new BetThresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, PLAYER_TIME, INITIAL_DELAY), new PlayerRange(1, 7), Type.PUBLIC, 7, UUID.randomUUID());
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		table.watch(blackjackPlayer);
		Assertions.assertEquals(1, table.getWatchers().size());
		Assertions.assertEquals(0, table.getPlayers().size());
	}

	@Test
	public void takingSeatChangesWatcherToPlayer() {
		BlackjackTable table = new BlackjackTable(Status.WAITING_PLAYERS, new BetThresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, PLAYER_TIME, INITIAL_DELAY), new PlayerRange(1, 7), Type.PUBLIC, 7, UUID.randomUUID());
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		table.trySeat(0, blackjackPlayer);
		Assertions.assertEquals(0, table.getWatchers().size());
		Assertions.assertEquals(1, table.getPlayers().size());
	}

	@Test
	public void reservedSeatCannotBeTaken() {
		BlackjackTable table = new BlackjackTable(Status.WAITING_PLAYERS, new BetThresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, PLAYER_TIME, INITIAL_DELAY), new PlayerRange(1, 7), Type.PUBLIC, 7, UUID.randomUUID());
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		BlackjackPlayer blackjackPlayer2 = new BlackjackPlayer("JaneDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		table.trySeat(0, blackjackPlayer);
		table.trySeat(0, blackjackPlayer2);
		Assertions.assertEquals(blackjackPlayer, table.getSeats().stream().filter(seat -> seat.getPlayer() != null).findFirst().get().getPlayer());
	}

	@Test
	public void playerCannotTakeSeatIfMinimumBetIsNotCovered() {
		BlackjackTable table = new BlackjackTable(Status.WAITING_PLAYERS, new BetThresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, PLAYER_TIME, INITIAL_DELAY), new PlayerRange(1, 7), Type.PUBLIC, 7, UUID.randomUUID());
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("4.99"), table);
		assertThrows(IllegalArgumentException.class, () -> {
			table.trySeat(0, blackjackPlayer);
		});
		Assertions.assertEquals(0, table.getPlayers().size());
	}

	@Test
	public void seatsAreCreatedPerSeatRequirement() {
		BlackjackTable table = new BlackjackTable(Status.WAITING_PLAYERS, new BetThresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, PLAYER_TIME, INITIAL_DELAY), new PlayerRange(1, 6), Type.PUBLIC, 15, UUID.randomUUID());
		Assertions.assertEquals(15, table.getSeats().size());
	}

	@Test
	public void exceptionIsThrownIfNotEnoughSeatsForMaximumAmountPlayers() {
		String expectedMessage = "not enough seats for the players";
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			new BlackjackTable(Status.WAITING_PLAYERS, new BetThresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, PLAYER_TIME, INITIAL_DELAY), new PlayerRange(1, 6), Type.PUBLIC, 2, UUID.randomUUID());
		});
		String actualMessage = exception.getMessage();
		assertEquals(expectedMessage, actualMessage);
	}
}
