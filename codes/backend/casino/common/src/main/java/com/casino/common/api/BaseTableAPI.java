package com.casino.common.api;

import java.util.UUID;

import com.casino.common.table.TableCard;
import com.casino.common.table.TableStatus;

public interface BaseTableAPI {

	TableCard getTableCard();

	TableStatus getStatus();

	UUID getId();
}
