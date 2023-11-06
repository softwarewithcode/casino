package com.casino.service.table;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.casino.common.api.TableAPI;
import com.casino.common.game.GameData;
import com.casino.common.table.TableCard;

public interface TableService {

	<T> Optional<? extends TableAPI> fetchTable(UUID id);

	List<TableCard<? extends GameData>> fetchTableCards(Integer from, Integer till);
}
