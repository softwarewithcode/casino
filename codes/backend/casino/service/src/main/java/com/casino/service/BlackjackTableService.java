package com.casino.service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.casino.blackjack.ext.IBlackjackTable;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.table.Status;
import com.casino.common.table.Thresholds;
import com.casino.common.table.Type;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BlackjackTableService {
	private static final Logger LOGGER = Logger.getLogger(BlackjackTableService.class.getName());
	private static final BigDecimal MIN_BET_DEFAULT = new BigDecimal("5.0");
	private static final BigDecimal MAX_BET_DEFAULT = new BigDecimal("100.0");
	private static final Integer BET_PHASE_TIME_SECONDS_DEFAULT = 15;
	private static final Integer INSURANCE_PHASE_TIME_SECONDS_DEFAULT = 11;
	private static final Integer PLAYER_TIME_SECONDS_DEFAULT = 30;
	private static final long DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS_DEFAULT = 10000l;
	private static final Integer MIN_PLAYER_DEFAULT = 0;
	private static final Integer MAX_PLAYERS_DEFAULT = 7;
	private static final Integer SEAT_DCOUNT_DEFAULT = 7;

	private final ConcurrentHashMap<UUID, BlackjackTable> tables = new ConcurrentHashMap<>();

	public BlackjackTableService() {
		super();
		BlackjackTable table = new BlackjackTable(Status.WAITING_PLAYERS, new Thresholds(MIN_BET_DEFAULT, MAX_BET_DEFAULT, BET_PHASE_TIME_SECONDS_DEFAULT, INSURANCE_PHASE_TIME_SECONDS_DEFAULT, PLAYER_TIME_SECONDS_DEFAULT,
				DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS_DEFAULT, MIN_PLAYER_DEFAULT, MAX_PLAYERS_DEFAULT, SEAT_DCOUNT_DEFAULT, Type.PUBLIC), UUID.fromString("e021c3bf-ffd9-4f75-953f-61639222e50d"));
		tables.putIfAbsent(table.getId(), table);
	}

	public void monitorTables() {
		// create new tables as tables get full
		// delete closed status tables
	}

	public void createTable(Status status, Thresholds thresholds) {
		UUID id = UUID.randomUUID();
		BlackjackTable table = new BlackjackTable(status, thresholds, id);
		tables.putIfAbsent(id, table);
	}

	public Optional<IBlackjackTable> fetchTable(UUID id) {
		if (id == null)
			throw new IllegalArgumentException("table id is required:" + id);
		return Optional.ofNullable(tables.get(id));
	}
}
