package com.casino.blackjack.test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;

import com.casino.blackjack.player.BlackjackPlayer;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.bet.BetValues;
import com.casino.common.player.ICasinoPlayer;
import com.casino.common.table.ISeatedTable;
import com.casino.common.table.PlayerRange;
import com.casino.common.table.Status;
import com.casino.common.table.Type;

public class BaseTest {
	protected static final BigDecimal MIN_BET = new BigDecimal("5.0");
	protected static final BigDecimal MAX_BET = new BigDecimal("100.0");
	protected static final Integer BET_ROUND_TIME_SECONDS = 2;
	protected static final Integer INDIVIDUAL_BET_TIME = 10;
	protected static final Integer INITIAL_DELAY = 0;
	protected ISeatedTable publicTable;
	protected ICasinoPlayer blackjackPlayer;
	protected ICasinoPlayer blackjackPlayer2;

	@BeforeEach
	public void init() {
		System.out.println("Init-all-tests-beforeEach");
		publicTable = createPublicTable();
		blackjackPlayer = new BlackjackPlayer("JohnDoe", UUID.randomUUID(), new BigDecimal("100.0"));
		blackjackPlayer2 = new BlackjackPlayer("JaneDoe", UUID.randomUUID(), new BigDecimal("100.0"));
	}

	protected BlackjackTable createPublicTable() {
		return new BlackjackTable(Status.OPEN, new BetValues(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INDIVIDUAL_BET_TIME, INITIAL_DELAY), new PlayerRange(1, 7), Type.PUBLIC, 7, UUID.randomUUID());
	}

	protected boolean takeSeat(int seatNumber, ICasinoPlayer player) {
		return publicTable.takeSeat(seatNumber, player);
	}

	protected void sleep(int i, ChronoUnit unit) {
		try {
			Thread.sleep(Duration.of(i, unit));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
