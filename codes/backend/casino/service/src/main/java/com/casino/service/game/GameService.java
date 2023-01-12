package com.casino.service.game;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.casino.blackjack.ext.IBlackjackTable;
import com.casino.common.table.Status;
import com.casino.common.table.TableCard;
import com.casino.common.table.TableInitData;
import com.casino.common.user.Bridge;

public interface GameService {
	void onSocketClose(Bridge bridge);

	void createTable(Status status, TableInitData initData);

	Optional<IBlackjackTable> fetchTable(UUID id);

	List<TableCard> fetchTableCards(Integer from, Integer till);
}
