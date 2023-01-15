package com.casino.service.game;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.casino.blackjack.ext.IBlackjackTable;
import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.language.Language;
import com.casino.common.table.Game;
import com.casino.common.table.Status;
import com.casino.common.table.TableCard;
import com.casino.common.table.TableInitData;
import com.casino.common.table.Thresholds;
import com.casino.common.table.Type;
import com.casino.common.user.Bridge;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BlackjackService implements GameService {
	private static final Integer BET_PHASE_TIME_SECONDS_DEFAULT = 15;
	private static final Integer INSURANCE_PHASE_TIME_SECONDS_DEFAULT = 11;
	private static final Integer PLAYER_TIME_SECONDS_DEFAULT = 30;
	private static final long DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS_DEFAULT = 14000l;
	private static final Integer MIN_PLAYER_DEFAULT = 0;
	private static final Integer MAX_PLAYERS_DEFAULT = 7;
	private static final Integer SEAT_COUNT_DEFAULT = 7;
	private static final Integer DEFAULT_ALLOWED_SIT_OUT_ROUNDS = 1;
	private final ConcurrentHashMap<UUID, BlackjackTable> tables = new ConcurrentHashMap<>();

	public BlackjackService() {
		createSomeTables();
	}

	@Override
	public void createTable(Status status, TableInitData initData) {
		UUID id = UUID.randomUUID();
		BlackjackTable table = new BlackjackTable(status, initData);
		tables.putIfAbsent(id, table);
	}

	@Override
	public Optional<IBlackjackTable> fetchTable(UUID id) {
		if (id == null)
			throw new IllegalArgumentException("table id is required:" + id);
		return Optional.ofNullable(tables.get(id));
	}

	@Override
	public void onSocketClose(Bridge bridge) {
		BlackjackTable table = tables.get(bridge.tableId());
		if (table == null)
			return;
		if (table.removeWatcher(bridge.userId()) != null)
			return;
		table.onPlayerLeave(bridge.userId());
	}

	public List<TableCard> fetchTableCards(Integer from, Integer till) {
		return tables.values().stream().filter(blackjackTable -> blackjackTable.getStatus().isVisible()).map(BlackjackTable::getTableCard).toList();
	}

	///////
	private void createSomeTables() {
		TableInitData tableInitData = createDefaultInitData(new BigDecimal("5"), new BigDecimal("10"), Type.MULTIPLAYER, MAX_PLAYERS_DEFAULT, SEAT_COUNT_DEFAULT);
		BlackjackTable temp1 = new BlackjackTable(Status.WAITING_PLAYERS, tableInitData);
		tables.putIfAbsent(temp1.getId(), temp1);
		TableInitData tableInitData2 = createDefaultInitData(new BigDecimal("5"), new BigDecimal("200"), Type.MULTIPLAYER, MAX_PLAYERS_DEFAULT, SEAT_COUNT_DEFAULT);
		BlackjackTable temp2 = new BlackjackTable(Status.WAITING_PLAYERS, tableInitData2);
		tables.putIfAbsent(temp2.getId(), temp2);
		TableInitData temp3 = createDefaultInitData(new BigDecimal("0.5"), new BigDecimal("1.0"), Type.MULTIPLAYER, MAX_PLAYERS_DEFAULT, SEAT_COUNT_DEFAULT);
		BlackjackTable table3 = new BlackjackTable(Status.WAITING_PLAYERS, temp3);
		tables.putIfAbsent(table3.getId(), table3);
		TableInitData temp4 = createDefaultInitData(new BigDecimal("50"), new BigDecimal("100"), Type.MULTIPLAYER, MAX_PLAYERS_DEFAULT, SEAT_COUNT_DEFAULT);
		BlackjackTable table4 = new BlackjackTable(Status.CLOSED, temp4);
		tables.putIfAbsent(table4.getId(), table4);
		TableInitData temp5 = createDefaultInitData(new BigDecimal("10.0"), new BigDecimal("100.0"), Type.SINGLE_PLAYER, 1, 5);
		BlackjackTable table5 = new BlackjackTable(Status.WAITING_PLAYERS, temp5);
		tables.putIfAbsent(table5.getId(), table5);

		TableInitData temp6 = createDefaultInitData(new BigDecimal("10.0"), new BigDecimal("100.0"), Type.MULTIPLAYER, 3, 7);
		BlackjackTable table6 = new BlackjackTable(Status.WAITING_PLAYERS, temp6);
		tables.putIfAbsent(table6.getId(), table6);

		TableInitData temp7 = createDefaultInitData(new BigDecimal("100.0"), new BigDecimal("500.0"), Type.MULTIPLAYER, 4, 4);
		BlackjackTable table7 = new BlackjackTable(Status.WAITING_PLAYERS, temp7);
		tables.putIfAbsent(table7.getId(), table7);

		TableInitData temp8 = createDefaultInitData(new BigDecimal("100.0"), new BigDecimal("10000.0"), Type.SINGLE_PLAYER, 3, 7);
		BlackjackTable table8 = new BlackjackTable(Status.WAITING_PLAYERS, temp8);
		tables.putIfAbsent(table8.getId(), table5);

		TableInitData temp9 = createDefaultInitData(new BigDecimal("1000.0"), new BigDecimal("100000"), Type.MULTIPLAYER, 1, 7);
		BlackjackTable table9 = new BlackjackTable(Status.WAITING_PLAYERS, temp9);
		tables.putIfAbsent(table9.getId(), table9);

		// Fast table
		TableInitData temp10 = createFastTable(new BigDecimal("1000.0"), new BigDecimal("10000"), Type.MULTIPLAYER, 1, 7);
		BlackjackTable table10 = new BlackjackTable(Status.WAITING_PLAYERS, temp10);
		tables.putIfAbsent(table10.getId(), table10);
		// Fast table2
		TableInitData temp11 = createFastTable(new BigDecimal("100"), new BigDecimal("200"), Type.MULTIPLAYER, 1, 7);
		BlackjackTable table11 = new BlackjackTable(Status.WAITING_PLAYERS, temp11);
		tables.putIfAbsent(table11.getId(), table11);
		TableInitData temp12 = createFastTable2(new BigDecimal("10"), new BigDecimal("100"), Type.MULTIPLAYER, 1, 7);
		BlackjackTable table12 = new BlackjackTable(Status.WAITING_PLAYERS, temp12);
		tables.putIfAbsent(table11.getId(), table12);
	}

	private TableInitData createDefaultInitData(BigDecimal min, BigDecimal max, Type type, Integer maxPlayer, Integer seatCount) {
		Thresholds thresholds = new Thresholds(min, max, BET_PHASE_TIME_SECONDS_DEFAULT, INSURANCE_PHASE_TIME_SECONDS_DEFAULT, PLAYER_TIME_SECONDS_DEFAULT, DELAY_BEFORE_STARTING_NEW_BET_PHASE_MILLIS_DEFAULT, MIN_PLAYER_DEFAULT, maxPlayer,
				seatCount, DEFAULT_ALLOWED_SIT_OUT_ROUNDS);
		TableInitData tableInitData = new TableInitData(thresholds, UUID.randomUUID(), Language.ENGLISH, type, Game.BLACKJACK);
		return tableInitData;
	}

	private TableInitData createFastTable(BigDecimal min, BigDecimal max, Type type, Integer maxPlayer, Integer seatCount) {
		Thresholds thresholds = new Thresholds(min, max, 7, 6, 10, 5000l, MIN_PLAYER_DEFAULT, maxPlayer, seatCount, DEFAULT_ALLOWED_SIT_OUT_ROUNDS);
		TableInitData tableInitData = new TableInitData(thresholds, UUID.randomUUID(), Language.ENGLISH, type, Game.BLACKJACK);
		return tableInitData;
	}

	private TableInitData createFastTable2(BigDecimal min, BigDecimal max, Type type, Integer maxPlayer, Integer seatCount) {
		Thresholds thresholds = new Thresholds(min, max, 8, 7, 11, 5000l, MIN_PLAYER_DEFAULT, maxPlayer, seatCount, DEFAULT_ALLOWED_SIT_OUT_ROUNDS);
		TableInitData tableInitData = new TableInitData(thresholds, UUID.randomUUID(), Language.ENGLISH, type, Game.BLACKJACK);
		return tableInitData;
	}

}
