package com.casino.blackjack.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.table.Status;
import com.casino.common.table.Thresholds;
import com.casino.common.table.Type;

public class SeatTest extends BaseTest {

	@Test
	public void initialTableJoinSetsPlayerAsWatcherInPublicTable() {
		BlackjackTable table = createNewTable();
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		table.watch(blackjackPlayer);
		Assertions.assertEquals(1, table.getWatchers().size());
		Assertions.assertEquals(0, table.getPlayers().size());
	}

	private BlackjackTable createNewTable() {
		return new BlackjackTable(Status.WAITING_PLAYERS,
				new Thresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS, MIN_PLAYERS, MAX_PLAYERS, DEFAULT_SEAT_COUNT, Type.PUBLIC),
				UUID.randomUUID());
	}

	@Test
	public void takingSeatChangesWatcherToPlayer() {
		BlackjackTable table = createNewTable();
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 0);
		Assertions.assertEquals(0, table.getWatchers().size());
		Assertions.assertEquals(1, table.getPlayers().size());
	}

	@Test
	public void reservedSeatCannotBeTaken() {
		BlackjackTable table = createNewTable();
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		BlackjackPlayer blackjackPlayer2 = new BlackjackPlayer("JaneDoe", UUID.randomUUID(), new BigDecimal("1000"), table);
		table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 0);
		table.join(blackjackPlayer2.getId(), blackjackPlayer2.getName(), blackjackPlayer2.getBalance(), 0);
		Assertions.assertEquals(blackjackPlayer, table.getSeats().stream().filter(seat -> seat.getPlayer() != null).findFirst().get().getPlayer());
	}

	@Test
	public void playerCannotTakeSeatIfMinimumBetIsNotCovered() {
		BlackjackTable table = createNewTable();
		BlackjackPlayer blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("4.99"), table);
		assertThrows(IllegalArgumentException.class, () -> {
			table.join(blackjackPlayer.getId(), blackjackPlayer.getName(), blackjackPlayer.getBalance(), 0);
		});
		Assertions.assertEquals(0, table.getPlayers().size());
	}

	@Test
	public void seatsAreCreatedPerSeatRequirement() {
		BlackjackTable table = new BlackjackTable(Status.WAITING_PLAYERS,
				new Thresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS, MIN_PLAYERS, 6, 15, Type.PUBLIC), UUID.randomUUID());
		Assertions.assertEquals(15, table.getSeats().size());
	}

	@Test
	public void exceptionIsThrownIfNotEnoughSeatsForMaximumAmountPlayers() {
		String expectedMessage = "not enough seats for the players";
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			new BlackjackTable(Status.WAITING_PLAYERS, new Thresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS, MIN_PLAYERS, 6, 2, Type.PUBLIC),
					UUID.randomUUID());
		});
		String actualMessage = exception.getMessage();
		assertEquals(expectedMessage, actualMessage);
	}
}
