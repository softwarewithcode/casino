package com.casino.common.api;

import java.util.UUID;

import com.casino.common.table.TableCard;
import com.casino.common.table.TableStatus;
import com.casino.common.user.User;

public interface BaseTableAPI {

	TableCard getTableCard();

	TableStatus getStatus();

	UUID getId();
	
	void watch(User user);

}
