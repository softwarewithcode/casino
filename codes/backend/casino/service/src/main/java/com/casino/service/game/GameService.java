package com.casino.service.game;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.casino.common.api.BaseTableAPI;
import com.casino.common.table.TableCard;

public interface GameService {

	<T> Optional<? extends BaseTableAPI> fetchTable(UUID id);

	List<TableCard> fetchTableCards(Integer from, Integer till);
}
