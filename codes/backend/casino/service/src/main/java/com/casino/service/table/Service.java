package com.casino.service.table;

import com.casino.common.api.TableAPI;
import com.casino.common.game.GameData;
import com.casino.common.table.TableCard;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Service implements TableService {

    abstract Collection<? extends TableAPI> getTables(Integer from, Integer till);

    @Override
    public List<TableCard<? extends GameData>> fetchTableCards(Integer from, Integer till) {
        return getTables(from, till).stream()
                .filter(api -> api.getStatus().isVisible())
                .map(TableAPI::getTableCard)
                .collect(Collectors.toList());
    }
}
