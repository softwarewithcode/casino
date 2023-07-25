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
import com.casino.common.user.User;

public class SeatTest extends BaseTest {
	private BlackjackTable table;
	private BlackjackTable singlePlayerTable;
	private User user4;
	private User user5;
	private User user6;
	private User user7;

	@BeforeEach
	public void initTest() {
		try {
			table = new BlackjackTable(getDefaultTableInitData(), blackjackInitData);
			singlePlayerTable = new BlackjackTable(getDefaultSinglePlayerTableInitData(), blackjackInitData);
			user = new User("JohnDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
			user2 = new User("JaneDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
			user3 = new User("JaneDoe2", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
			user4 = new User("JaneDoe3", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
			user5 = new User("JaneDoe4", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
			user6 = new User("JaneDoe5", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
			user7 = new User("JaneDoe6", table.getId(), UUID.randomUUID(), null, new BigDecimal("1000.0"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void initialTableJoinSetsPlayerAsWatcherInPublicTable() {
		table.watch(user);
		Assertions.assertEquals(1, table.getWatchers().size());
		Assertions.assertEquals(0, table.getPlayers().size());
	}

	@Test
	public void takingSeatChangesWatcherToPlayer() {
		table.join(user, "0");
		Assertions.assertEquals(0, table.getWatchers().size());
		Assertions.assertEquals(1, table.getPlayers().size());
	}

	@Test
	public void reservedSeatCannotBeTaken() {
		table.join(user, "0");
		assertFalse(table.join(user2, "0"));
		Assertions.assertEquals(user.userId(), table.getSeats().stream().filter(seat -> seat.getPlayer() != null).findFirst().get().getPlayer().getId());
	}

	@Test
	public void playerCannotTakeSeatIfMinimumBetIsNotCovered() {
		User b = new User("JohnDoe", table.getId(), UUID.randomUUID(), null, new BigDecimal("4.99"));
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
		singlePlayerTable.join(user, "0");
		Assertions.assertEquals(1, singlePlayerTable.getPlayers().size());
		assertFalse(singlePlayerTable.join(user2, "1"));

	}

	@Test
	public void samePlayerIsNotAllowedToJoinAgainInSinglePlayerTable() {
		singlePlayerTable.join(user, "0");
		Assertions.assertEquals(1, singlePlayerTable.getPlayers().size());
		assertFalse(singlePlayerTable.join(user, "1"));
	}

	@Test
	public void searchAnyFreeSeatFindsLastFreeSeat() {
		table.join(user, "0");
		table.join(user2, "1");
		table.join(user3, "3");
		table.join(user4, "4");
		table.join(user5, "5");
		table.join(user6, "6");
		assertTrue(table.join(user7, null));
		Seat expectedSeat = table.getSeats().stream().filter(seat -> seat.getNumber() == 2).findAny().get();
		assertTrue(expectedSeat.getPlayer().getId().equals(user7.userId()));
	}
}
