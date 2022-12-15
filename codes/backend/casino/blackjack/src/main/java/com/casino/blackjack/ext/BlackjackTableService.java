package com.casino.blackjack.ext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.table.Status;
import com.casino.common.table.Thresholds;
import com.casino.common.table.Type;

public class BlackjackTableService {
	private static final BigDecimal MIN_BET = new BigDecimal("5.0");
	private static final BigDecimal MAX_BET = new BigDecimal("100.0");
	private static final Integer BET_ROUND_TIME_SECONDS = 20;
	private static final Integer INSURANCE_ROUND_TIME_SECONDS = 3;
	private static final Integer PLAYER_TIME_SECONDS = 30;
	private static final long DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS = 5000l;
	private static final Integer MIN_PLAYERS = 0;
	private static final Integer MAX_PLAYERS = 7;
	private static final Integer DEFAULT_SEAT_COUNT = 7;

	private List<BlackjackTable> tables = new ArrayList<>();

	public BlackjackTableService() {
		super();
		BlackjackTable table = new BlackjackTable(Status.WAITING_PLAYERS,
				new Thresholds(MIN_BET, MAX_BET, BET_ROUND_TIME_SECONDS, INSURANCE_ROUND_TIME_SECONDS, PLAYER_TIME_SECONDS, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS, MIN_PLAYERS, MAX_PLAYERS, DEFAULT_SEAT_COUNT, Type.PUBLIC),
				UUID.fromString("e021c3bf-ffd9-4f75-953f-61639222e50d"));
		tables.add(table);
	}

	public void monitorTables() {
		// create new tables as tables get full?
	}

	public BlackjackReverseProxy getTable(UUID id) {
		if (id == null)
			throw new IllegalArgumentException("no such table:" + id);
		return tables.stream().filter(table -> table.getId().equals(id) && table.isOpen()).findAny().orElse(null);
	}
}
