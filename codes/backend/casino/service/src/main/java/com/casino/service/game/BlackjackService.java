package com.casino.service.game;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.casino.blackjack.export.BlackjackAPI;
import com.casino.blackjack.export.BlackjackTableFactory;
import com.casino.common.table.TableCard;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BlackjackService implements GameService {

	private final ConcurrentHashMap<UUID, BlackjackAPI> blackjackTables = new ConcurrentHashMap<>();

	public BlackjackService() {
		BlackjackAPI single = BlackjackTableFactory.createDefaultSinglePlayerTable();
		BlackjackAPI multi = BlackjackTableFactory.createDefaultMultiplayerTable();
		blackjackTables.put(single.getId(), single);
		blackjackTables.put(multi.getId(), multi);
	}

	@Override
	public Optional<BlackjackAPI> fetchTable(UUID id) {
		if (id == null)
			throw new IllegalArgumentException("table id is required:");
		return Optional.ofNullable(blackjackTables.get(id));
	}



	@Override
	public List<TableCard> fetchTableCards(Integer from, Integer till) {
		return blackjackTables.values().stream().filter(blackjackAPI -> blackjackAPI.getStatus().isVisible()).map(BlackjackAPI::getTableCard).toList();
	}

}
