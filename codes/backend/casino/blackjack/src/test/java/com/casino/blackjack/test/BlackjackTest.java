package com.casino.blackjack.test;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.bet.BetValues;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.table.ISeatedTable;
import com.casino.common.table.PlayerRange;
import com.casino.common.table.Status;
import com.casino.common.table.Type;

public class BlackjackTest {
	private ISeatedTable table;
	private ICasinoPlayer blackjackPlayer;
	private ICasinoPlayer blackjackPlayer2;
	private static final BigDecimal MIN_BET = new BigDecimal("5.0");
	private static final BigDecimal MAX_BET = new BigDecimal("100.0");
	private static final Integer BET_ROUND_TIME = 10;
	private static final Integer INDIVIDUAL_BET_TIME = 10;

	@BeforeEach
	public void init() {
		table = createPublicTable();
		blackjackPlayer = new BlackjackPlayer("PlayerName", UUID.randomUUID(), new BigDecimal("100.0"));
		blackjackPlayer2 = new BlackjackPlayer("Name", UUID.randomUUID(), new BigDecimal("100.0"));
	}

	private BlackjackTable createPublicTable() {
		return new BlackjackTable(Status.OPEN, new BetValues(MIN_BET, MAX_BET, BET_ROUND_TIME, INDIVIDUAL_BET_TIME), new PlayerRange(1, 7), Type.PUBLIC, 7, UUID.randomUUID());
	}

	@Test
	public void initialTableJoinSetsPlayerAsWatcherInPublicTable() {
		table.join(blackjackPlayer);
		Assertions.assertEquals(1, table.getWatchers().size());
		Assertions.assertEquals(0, table.getPlayers().size());
	}

	@Test
	public void takingSeatChangesWatcherToPlayer() {
		takeSeat(0, blackjackPlayer);
		Assertions.assertEquals(0, table.getWatchers().size());
		Assertions.assertEquals(1, table.getPlayers().size());
	}

	@Test
	public void reservedSeatCannotBeTaken() {
		takeSeat(0, blackjackPlayer);
		takeSeat(0, blackjackPlayer2);
		Assertions.assertEquals(blackjackPlayer, table.getSeats().stream().filter(seat -> seat.getPlayer() != null).findFirst().get().getPlayer());
	}

	private void takeSeat(int seatNumber, ICasinoPlayer player) {
		table.join(player);
		table.takeSeat(seatNumber, player);
	}

}
