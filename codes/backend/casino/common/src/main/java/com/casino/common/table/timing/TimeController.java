package com.casino.common.table.timing;

import java.util.UUID;

public interface TimeController {
	void updateCounterTime(Integer time);

	Integer getCounterTime();

	boolean isClockTicking();

	void stopClock();

	UUID getTableId();

}
