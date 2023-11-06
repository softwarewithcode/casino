package com.casino.common.api;

import java.util.UUID;

import com.casino.common.game.GameData;
import com.casino.common.table.TableCard;
import com.casino.common.table.TableStatus;
import com.casino.common.user.Connectable;

public interface TableAPI {

	TableCard<? extends GameData> getTableCard();

	TableStatus getStatus();

	UUID getId();

	void watch(Connectable user);

	void leave(UUID id);

	void refresh(UUID id);
}
