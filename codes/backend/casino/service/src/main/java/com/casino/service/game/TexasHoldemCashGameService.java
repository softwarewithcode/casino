package com.casino.service.game;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.casino.common.table.TableCard;
import com.casino.poker.export.HoldemTableFactory;
import com.casino.poker.export.NoLimitTexasHoldemAPI;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TexasHoldemCashGameService implements GameService {
	private final ConcurrentHashMap<UUID, NoLimitTexasHoldemAPI> texasHoldemTables = new ConcurrentHashMap<>();

	public TexasHoldemCashGameService() {
		createSomeTables();
	}

	private void createSomeTables() {
		NoLimitTexasHoldemAPI holdemTable = HoldemTableFactory.createDefaultTexasHoldemCashGameTable();
		NoLimitTexasHoldemAPI holdemTable2 = HoldemTableFactory.createDefaultTexasHoldemCashGameTable();
		NoLimitTexasHoldemAPI holdemTable3 = HoldemTableFactory.createDefaultTexasHoldemCashGameTable();
		NoLimitTexasHoldemAPI holdemTable4 = HoldemTableFactory.createDefaultTexasHoldemCashGameTable();
		NoLimitTexasHoldemAPI holdemTable5 = HoldemTableFactory.createDefaultTexasHoldemCashGameTable();
		texasHoldemTables.put(holdemTable.getId(), holdemTable);
		texasHoldemTables.put(holdemTable2.getId(), holdemTable2);
		texasHoldemTables.put(holdemTable3.getId(), holdemTable3);
		
		texasHoldemTables.put(holdemTable4.getId(), holdemTable4);
		texasHoldemTables.put(holdemTable5.getId(), holdemTable5);
	}

	@Override
	public Optional<NoLimitTexasHoldemAPI> fetchTable(UUID tableId) {
		if (tableId == null)
			throw new IllegalArgumentException("table id is required:");
		return Optional.ofNullable(texasHoldemTables.get(tableId));
	}

	@Override
	public List<TableCard> fetchTableCards(Integer from, Integer till) {
		return texasHoldemTables.values().stream().filter(holdemAPI -> holdemAPI.getStatus().isVisible()).map(NoLimitTexasHoldemAPI::getTableCard).toList();
	}
}
