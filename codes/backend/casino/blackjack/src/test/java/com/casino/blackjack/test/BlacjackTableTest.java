package com.casino.blackjack.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.bet.BetValues;
import com.casino.common.table.PlayerRange;
import com.casino.common.table.Status;
import com.casino.common.table.Type;

public class BlacjackTableTest {

	private static final BigDecimal MIN_BET = new BigDecimal("5.0");
	private static final BigDecimal MAX_BET = new BigDecimal("100.0");
	private static final Integer BET_ROUND_TIME = 10;
	private static final Integer INDIVIDUAL_BET_TIME = 10;

	@Test
	public void tableUsesSixDecks() {
		BlackjackTable table = new BlackjackTable(Status.OPEN, new BetValues(MIN_BET, MAX_BET, BET_ROUND_TIME, INDIVIDUAL_BET_TIME), new PlayerRange(1, 1), Type.PRIVATE, 7, UUID.randomUUID());
		Assertions.assertEquals(312, table.getDecks().size());
	}

	@Test
	public void seatsAreCreatedPerSeatRequirement() {
		BlackjackTable table = new BlackjackTable(Status.OPEN, new BetValues(MIN_BET, MAX_BET, BET_ROUND_TIME, INDIVIDUAL_BET_TIME), new PlayerRange(1, 6), Type.PUBLIC, 15, UUID.randomUUID());
		Assertions.assertEquals(15, table.getSeats().size());
	}

	@Test
	public void exceptionIsThrownIfNotEnoughSeatsForMaximumAmountPlayers() {
		String expectedMessage = "not enough seats for the players";
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			new BlackjackTable(Status.OPEN, new BetValues(MIN_BET, MAX_BET, BET_ROUND_TIME, INDIVIDUAL_BET_TIME), new PlayerRange(1, 6), Type.PUBLIC, 2, UUID.randomUUID());
		});
		String actualMessage = exception.getMessage();
		assertEquals(expectedMessage, actualMessage);
	}
}
