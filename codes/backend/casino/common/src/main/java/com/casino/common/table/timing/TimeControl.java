package com.casino.common.table.timing;

public interface TimeControl {

	Integer getTimeBank();

	Integer increaseTimeBank(Integer amount);

	Integer reduceSecond(boolean timeBankAllowed);

	Integer getTotalTime();

	Integer getPlayerTime();

	void initPlayerTime(Integer time);

}
