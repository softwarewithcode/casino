package com.casino.common.table.timing;

public class Time implements TimeControl {
    private Integer turnTime;
    private Integer extraTime;

    public Time(Integer playerTime, Integer extraTime) {
        super();
        this.turnTime = playerTime;
        this.extraTime = extraTime;
    }

    @Override
    public Integer getAdditionalTime() {
        return extraTime;
    }

    @Override
    public Integer increaseAdditionalTime(Integer amount) {
        extraTime += amount;
        return extraTime;
    }

    @Override
    public Integer getTotalTime() {
        return turnTime + extraTime;
    }

    @Override
    public Integer reduceSecond(boolean useTimeBank) {
        if (turnTime > 0) {
            turnTime--;
            return turnTime;
        }
        if (useTimeBank) {
            extraTime--;
            return extraTime;
        }
        return 0;
    }

    @Override
    public Integer getTurnTime() {
        return turnTime;
    }

    @Override
    public void setPlayerTime(Integer time) {
        this.turnTime = time;

    }

}
