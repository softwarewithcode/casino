package com.casino.blackjack.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.casino.common.table.structure.Seat;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.table.TableData;
import com.casino.common.table.TableThresholds;
import com.casino.common.user.Bridge;

public class SeatTest extends BaseTest {
	private BlackjackTable table;
	private BlackjackTable singlePlayerTable;
	private Bridge bridge4;
	private Bridge bridge5;
	private Bridge bridge6;
	private Bridge bridge7;

	@BeforeEach
	public void initTest() {
		try {
			table = new BlackjackTable(getDefaultTableInitData(), blackjackInitData);
			singlePlayerTable = new BlackjackTable(getDefaultSinglePlayerTableInitData(), blackjackInitData);
			bridge = new Bridge("JohnDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
			bridge2 = new Bridge("JaneDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
			bridge3 = new Bridge("JaneDoe2", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
			bridge4 = new Bridge("JaneDoe3", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
			bridge5 = new Bridge("JaneDoe4", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
			bridge6 = new Bridge("JaneDoe5", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
			bridge7 = new Bridge("JaneDoe6", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void initialTableJoinSetsPlayerAsWatcherInPublicTable() {
		table.watch(bridge);
		Assertions.assertEquals(1, table.getWatchers().size());
		Assertions.assertEquals(0, table.getPlayers().size());
	}

	@Test
	public void takingSeatChangesWatcherToPlayer() {
		table.join(bridge, "0");
		Assertions.assertEquals(0, table.getWatchers().size());
		Assertions.assertEquals(1, table.getPlayers().size());
	}

	@Test
	public void reservedSeatCannotBeTaken() {
		table.join(bridge, "0");
		assertFalse(table.join(bridge2, "0"));
		Assertions.assertEquals(bridge.userId(), table.getSeats().stream().filter(seat -> seat.getPlayer() != null).findFirst().get().getPlayer().getId());
	}

	@Test
	public void playerCannotTakeSeatIfMinimumBetIsNotCovered() {
		Bridge b = new Bridge("JohnDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("4.99"));
		assertThrows(IllegalArgumentException.class, () -> {
			table.join(b, "0");
		});
		Assertions.assertEquals(0, table.getPlayers().size());
	}

	@Test
	public void seatsAreCreatedPerSeatRequirement() {
		TableThresholds thresholds = new TableThresholds(MIN_PLAYERS, MAX_PLAYERS, 15);
		TableData tableInitData = getDefaultTableInitDataWithThresholds(thresholds);
		table = new BlackjackTable(tableInitData, blackjackInitData);
		Assertions.assertEquals(15, table.getSeats().size());
	}

	@Test
	public void exceptionIsThrownIfNotEnoughSeatsForMaximumAmountPlayers() {
		assertThrows(IllegalArgumentException.class, () -> new TableThresholds(MIN_PLAYERS, MAX_PLAYERS, 2));
	}

	@Test
	public void secondPlayerIsNotAllowedToJoinInSinglePlayerTable() {
		singlePlayerTable.join(bridge, "0");
		Assertions.assertEquals(1, singlePlayerTable.getPlayers().size());
		assertFalse(singlePlayerTable.join(bridge2, "1"));

	}

	@Test
	public void samePlayerIsNotAllowedToJoinAgainInSinglePlayerTable() {
		singlePlayerTable.join(bridge, "0");
		Assertions.assertEquals(1, singlePlayerTable.getPlayers().size());
		assertFalse(singlePlayerTable.join(bridge, "1"));
	}

	@Test
	public void searchAnyFreeSeatFindsLastFreeSeat() {
		table.join(bridge, "0");
		table.join(bridge2, "1");
		table.join(bridge3, "3");
		table.join(bridge4, "4");
		table.join(bridge5, "5");
		table.join(bridge6, "6");
		assertTrue(table.join(bridge7, null));
		Seat expectedSeat = table.getSeats().stream().filter(seat -> seat.getNumber() == 2).findAny().get();
		assertTrue(expectedSeat.getPlayer().getId().equals(bridge7.userId()));
	}
}
