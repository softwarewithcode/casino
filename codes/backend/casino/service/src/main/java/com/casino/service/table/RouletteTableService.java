package com.casino.service.table;

import com.casino.common.api.TableAPI;
import com.casino.roulette.export.RouletteTableAPI;
import com.casino.roulette.export.RouletteTableFactory;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class RouletteTableService extends Service {
    private final ConcurrentHashMap<UUID, RouletteTableAPI> rouletteTables = new ConcurrentHashMap<>();

    public RouletteTableService() {
        RouletteTableAPI multi = RouletteTableFactory.createDefaultMultiplayerTable();
        rouletteTables.put(multi.getId(), multi);
        RouletteTableAPI multi2 = RouletteTableFactory.createDefaultMultiplayerTable();
        rouletteTables.put(multi2.getId(), multi2);
        RouletteTableAPI multi3 = RouletteTableFactory.createDefaultMultiplayerTable();
        rouletteTables.put(multi3.getId(), multi3);
        RouletteTableAPI multi4 = RouletteTableFactory.createDefaultMultiplayerTable();
        rouletteTables.put(multi4.getId(), multi4);
        RouletteTableAPI multi5 = RouletteTableFactory.createDefaultMultiplayerTable();
        rouletteTables.put(multi5.getId(), multi5);

        RouletteTableAPI single = RouletteTableFactory.createDefaultSinglePlayerTable();
        rouletteTables.put(single.getId(), single);

        RouletteTableAPI single2 = RouletteTableFactory.createDefaultSinglePlayerTable();
        rouletteTables.put(single2.getId(), single2);
        RouletteTableAPI single3 = RouletteTableFactory.createDefaultSinglePlayerTable();
        rouletteTables.put(single3.getId(), single3);
        RouletteTableAPI single4 = RouletteTableFactory.createHighRollerSinglePlayerTable();
        rouletteTables.put(single4.getId(), single4);
    }

    @Override
    public Optional<RouletteTableAPI> fetchTable(UUID id) {
        if (id == null)
            return Optional.empty();
        return Optional.ofNullable(rouletteTables.get(id));
    }

    @Override
    Collection<? extends TableAPI> getTables(Integer from, Integer till) {
        return rouletteTables.values();
    }
}
