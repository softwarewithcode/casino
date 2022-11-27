package com.casino.blackjack.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.table.Status;
import com.casino.common.table.Type;

public class BlacjackTableTest {

	@Test
	public void tableUsesSixDecks() {
		BlackjackTable table = new BlackjackTable(Status.OPEN, new BigDecimal("5.0"), new BigDecimal("100.0"), 1, 1, Type.PRIVATE, 7, UUID.randomUUID());
		Assertions.assertEquals(312, table.getDecks().size());
	}

	@Test
	public void seatsAreCreatedPerSeatRequirement() {
		BlackjackTable table = new BlackjackTable(Status.OPEN, new BigDecimal("5.0"), new BigDecimal("100.0"), 1, 6, Type.PUBLIC, 15, UUID.randomUUID());
		Assertions.assertEquals(15, table.getSeats().size());
	}

	@Test
	public void exceptionIsThrownIfNotEnoughSeatsForMaximumAmountPlayers() {
		String expectedMessage = "not enough seats for the players";
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			new BlackjackTable(Status.OPEN, new BigDecimal("5.0"), new BigDecimal("100.0"), 1, 6, Type.PUBLIC, 2, UUID.randomUUID());
		});
		String actualMessage = exception.getMessage();
		assertEquals(expectedMessage, actualMessage);
	}
}
