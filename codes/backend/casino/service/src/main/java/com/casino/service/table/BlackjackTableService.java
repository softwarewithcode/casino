package com.casino.service.table;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.casino.blackjack.export.BlackjackTableAPI;
import com.casino.blackjack.export.BlackjackTableFactory;
import com.casino.common.api.TableAPI;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BlackjackTableService extends Service {

    private final ConcurrentHashMap<UUID, BlackjackTableAPI> blackjackTables = new ConcurrentHashMap<>();

    public BlackjackTableService() {
        BlackjackTableAPI single = BlackjackTableFactory.createDefaultSinglePlayerTable();
        BlackjackTableAPI multi = BlackjackTableFactory.createDefaultMultiplayerTable();
        blackjackTables.put(single.getId(), single);
        blackjackTables.put(multi.getId(), multi);
    }

    @Override
    public Optional<BlackjackTableAPI> fetchTable(UUID id) {
        if (id == null)
            return Optional.empty();
        return Optional.ofNullable(blackjackTables.get(id));
    }

  
    @Override
    Collection<? extends TableAPI> getTables(Integer from, Integer till) {
        return blackjackTables.values();
    }
}
