package com.casino.blackjack.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.bet.BetValues;
import com.casino.common.table.PlayerRange;
import com.casino.common.table.Status;
import com.casino.common.table.Type;

public class BlacjackTableTest extends BaseTest {

	@Test
	public void tableUsesSixDecks() {
		BlackjackTable table = new BlackjackTable(Status.WAITING_PLAYERS, new BetValues(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INDIVIDUAL_BET_TIME, INITIAL_DELAY), new PlayerRange(1, 1), Type.PRIVATE, 7, UUID.randomUUID());
		Assertions.assertEquals(312, table.getDealer().getDecks().size());
	}

	@Test
	public void seatsAreCreatedPerSeatRequirement() {
		BlackjackTable table = new BlackjackTable(Status.WAITING_PLAYERS, new BetValues(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INDIVIDUAL_BET_TIME, INITIAL_DELAY), new PlayerRange(1, 6), Type.PUBLIC, 15, UUID.randomUUID());
		Assertions.assertEquals(15, table.getSeats().size());
	}

	@Test
	public void exceptionIsThrownIfNotEnoughSeatsForMaximumAmountPlayers() {
		String expectedMessage = "not enough seats for the players";
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			new BlackjackTable(Status.WAITING_PLAYERS, new BetValues(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INDIVIDUAL_BET_TIME, INITIAL_DELAY), new PlayerRange(1, 6), Type.PUBLIC, 2, UUID.randomUUID());
		});
		String actualMessage = exception.getMessage();
		assertEquals(expectedMessage, actualMessage);
	}
}
