package com.casino.web.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.casino.blackjack.ext.BlackjackTableProxy;
import com.casino.common.user.Bridge;
import com.casino.web.endpoint.handler.TableHandler;

public class TableHandlerTest {

	@Test
	public void test() {
		TableHandler handler = new TableHandler();
		UUID tableId = UUID.randomUUID();
		UUID playerId = UUID.randomUUID();
		BlackjackTableProxy table = handler.fetchTable(tableId);
		Bridge bridge = new Bridge("JohnDoe", tableId, playerId, null, new BigDecimal("1000.0"));
		boolean joined = table.join(bridge, "0");
		table.bet(bridge.playerId(), new BigDecimal("1000.0"));
		assertTrue(joined);
	}
}
