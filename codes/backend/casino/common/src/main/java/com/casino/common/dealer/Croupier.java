package com.casino.common.dealer;

import java.util.UUID;

import com.casino.common.game.GameData;
import com.casino.common.player.CasinoPlayer;
import com.casino.common.table.TableStatus;
import com.casino.common.table.structure.CasinoTable;
import com.casino.common.user.Connectable;

//Basic functionalities that all Croupiers should implement. Dealers are specialized Croupiers within card games,
public interface Croupier {
	CasinoTable getTable();

	<T extends CasinoPlayer> void onPlayerArrival(T player);

	<T extends CasinoPlayer> void onPlayerLeave(T player);

	void onWatcherArrival(Connectable watcher);
	void refresh(Connectable connectable);

	default UUID getTableId() {
		return getTable().getId();
	}

	GameData getGameData();

	default void onError() {
		getTable().setStatus(TableStatus.ERROR);
	}

}
