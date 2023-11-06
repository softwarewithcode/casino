package com.casino.service.table;

import com.casino.common.api.TableAPI;
import com.casino.poker.export.HoldemTableFactory;
import com.casino.poker.export.NoLimitTexasHoldemAPI;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class TexasHoldemCashTableService extends Service {
    private final ConcurrentHashMap<UUID, NoLimitTexasHoldemAPI> texasHoldemTables = new ConcurrentHashMap<>();

    public TexasHoldemCashTableService() {
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
            return Optional.empty();
        return Optional.ofNullable(texasHoldemTables.get(tableId));
    }

    @Override
    Collection<? extends TableAPI> getTables(Integer from, Integer till) {
        return texasHoldemTables.values();
    }
}
