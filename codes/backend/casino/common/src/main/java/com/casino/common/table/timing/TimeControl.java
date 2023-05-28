package com.casino.common.table.timing;

public interface TimeControl {

	Integer getAdditionalTime();

	Integer increaseAdditionalTime(Integer amount);

	Integer reduceSecond(boolean timeBankable);

	Integer getTotalTime();

	Integer getTurnTime();

	void setPlayerTime(Integer time);

}
