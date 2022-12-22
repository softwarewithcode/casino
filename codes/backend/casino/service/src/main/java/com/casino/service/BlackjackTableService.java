package com.casino.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.casino.blackjack.ext.IBlackjackTable;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.language.Language;
import com.casino.common.table.Game;
import com.casino.common.table.Status;
import com.casino.common.table.TableDescription;
import com.casino.common.table.TableInitData;
import com.casino.common.table.Thresholds;
import com.casino.common.table.Type;
import com.casino.common.user.Bridge;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BlackjackTableService {
	private static final Logger LOGGER = Logger.getLogger(BlackjackTableService.class.getName());
	private static final Integer BET_PHASE_TIME_SECONDS_DEFAULT = 15;
	private static final Integer INSURANCE_PHASE_TIME_SECONDS_DEFAULT = 11;
	private static final Integer PLAYER_TIME_SECONDS_DEFAULT = 30;
	private static final long DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS_DEFAULT = 14000l;
	private static final Integer MIN_PLAYER_DEFAULT = 0;
	private static final Integer MAX_PLAYERS_DEFAULT = 7;
	private static final Integer SEAT_COUNT_DEFAULT = 7;

	private final ConcurrentHashMap<UUID, BlackjackTable> tables = new ConcurrentHashMap<>();

	public BlackjackTableService() {
		TableInitData tableInitData = createDefaultInitData(new BigDecimal("5"), new BigDecimal("10"));
		BlackjackTable table = new BlackjackTable(Status.WAITING_PLAYERS, tableInitData);
		tables.putIfAbsent(table.getId(), table);
		TableInitData tableInitData2 = createDefaultInitData(new BigDecimal("15"), new BigDecimal("100"));
		BlackjackTable table2 = new BlackjackTable(Status.WAITING_PLAYERS, tableInitData2);
		tables.putIfAbsent(table2.getId(), table2);
	}

	private TableInitData createDefaultInitData(BigDecimal min, BigDecimal max) {
		Thresholds thresholds = new Thresholds(min, max, BET_PHASE_TIME_SECONDS_DEFAULT, INSURANCE_PHASE_TIME_SECONDS_DEFAULT, PLAYER_TIME_SECONDS_DEFAULT, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS_DEFAULT, MIN_PLAYER_DEFAULT,
				MAX_PLAYERS_DEFAULT, SEAT_COUNT_DEFAULT);
		TableInitData tableInitData = new TableInitData(thresholds, UUID.randomUUID(), Language.ENGLISH, Type.PUBLIC, Game.BLACKJACK);
		return tableInitData;
	}

	public void monitorTables() {
		// create new tables as tables get full
		// delete closed status tables
	}

	public void createTable(Status status, TableInitData initData) {
		UUID id = UUID.randomUUID();
		BlackjackTable table = new BlackjackTable(status, initData);
		tables.putIfAbsent(id, table);
	}

	public Optional<IBlackjackTable> fetchTable(UUID id) {
		if (id == null)
			throw new IllegalArgumentException("table id is required:" + id);
		return Optional.ofNullable(tables.get(id));
	}

	public void removeWatcher(Bridge bridge) {
		BlackjackTable table = tables.get(bridge.tableId());
		table.removeWatcher(bridge.userId());
	}

	public List<TableDescription> fetchTableDescriptions() {
		return tables.values().stream().filter(table -> table.getStatus().isVisible()).map(BlackjackTable::getTableDescription).collect(Collectors.toList());
	}
}
