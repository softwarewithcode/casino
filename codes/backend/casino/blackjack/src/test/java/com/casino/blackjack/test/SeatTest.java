package com.casino.blackjack.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.table.Seat;
import com.casino.common.table.Status;
import com.casino.common.table.TableInitData;
import com.casino.common.table.Thresholds;
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
			table = new BlackjackTable(Status.WAITING_PLAYERS, getDefaultTableInitData());
			singlePlayerTable = new BlackjackTable(Status.WAITING_PLAYERS, getDefaultSinglePlayerTableInitData());
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
		Thresholds thresholds = new Thresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS, MIN_PLAYERS, 6, 15,DEFAULT_ALLOWED_SIT_OUT_ROUNDS);
		TableInitData tableInitData = getDefaultTableInitDataWithThresholds(thresholds);
		table = new BlackjackTable(Status.WAITING_PLAYERS, tableInitData);
		Assertions.assertEquals(15, table.getSeats().size());
	}

	@Test
	public void exceptionIsThrownIfNotEnoughSeatsForMaximumAmountPlayers() {
		String expectedMessage = "not enough seats for the players";
		Thresholds thresholds = new Thresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS, MIN_PLAYERS, 6, 2,DEFAULT_ALLOWED_SIT_OUT_ROUNDS);
		TableInitData tableInitData = getDefaultTableInitDataWithThresholds(thresholds);
		Exception exception = assertThrows(IllegalArgumentException.class, () -> table = new BlackjackTable(Status.WAITING_PLAYERS, tableInitData));
		String actualMessage = exception.getMessage();
		assertEquals(expectedMessage, actualMessage);
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
