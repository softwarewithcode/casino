package com.casino.blackjack.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.player.PlayerStatus;
import com.casino.common.table.Status;
import com.casino.common.table.TableInitData;
import com.casino.common.table.Thresholds;
import com.casino.common.user.Bridge;

public class SeatTest extends BaseTest {
	private BlackjackTable table;

	@BeforeEach
	public void initTest() {
		try {
			table = new BlackjackTable(Status.WAITING_PLAYERS, getDefaultTableInitData());
			bridge = new Bridge("JohnDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
			bridge2 = new Bridge("JaneDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
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
		table.join(bridge2, "0");
		Assertions.assertEquals(bridge.userId(), table.getSeats().stream().filter(seat -> seat.getPlayer() != null).findFirst().get().getPlayer().getId());
	}

	@Test
	public void playerCannotTakeSeatIfMinimumBetIsNotCovered() {
		Bridge b = bridge = new Bridge("JohnDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("4.99"));
		assertThrows(IllegalArgumentException.class, () -> {
			table.join(b, "0");
		});
		Assertions.assertEquals(0, table.getPlayers().size());
	}

	@Test
	public void seatsAreCreatedPerSeatRequirement() {
		Thresholds thresholds = new Thresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS, MIN_PLAYERS, 6, 15);
		TableInitData tableInitData = getDefaultTableInitDataWithThresholds(thresholds);
		table = new BlackjackTable(Status.WAITING_PLAYERS, tableInitData);
		Assertions.assertEquals(15, table.getSeats().size());
	}

	@Test
	public void exceptionIsThrownIfNotEnoughSeatsForMaximumAmountPlayers() {
		String expectedMessage = "not enough seats for the players";
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			Thresholds thresholds = new Thresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS, MIN_PLAYERS, 6, 2);
			TableInitData tableInitData = getDefaultTableInitDataWithThresholds(thresholds);
			table = new BlackjackTable(Status.WAITING_PLAYERS, tableInitData);
		});
		String actualMessage = exception.getMessage();
		assertEquals(expectedMessage, actualMessage);
	}
}
