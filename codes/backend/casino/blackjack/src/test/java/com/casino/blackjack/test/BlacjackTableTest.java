package com.casino.blackjack.test;

import java.math.BigDecimal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.table.Status;
import com.casino.common.table.Type;

public class BlacjackTableTest {

	@Test
	public void tableUsesSixDecks() {
		BlackjackTable table = new BlackjackTable(Status.OPEN, new BigDecimal("5.0"), new BigDecimal("100.0"), 1, 1, Type.PRIVATE, 7);
		Assertions.assertEquals(312, table.getDecks().size());
	}
}
