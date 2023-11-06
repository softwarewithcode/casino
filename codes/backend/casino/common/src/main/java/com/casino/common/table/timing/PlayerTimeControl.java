package com.casino.common.table.timing;

public class PlayerTimeControl implements TimeControl {
    private Integer playerTime;
    private Integer timeBank;

    public PlayerTimeControl(Integer playerTime, Integer timeBank) {
        super();
        this.playerTime = playerTime;
        this.timeBank = timeBank;
    }

    @Override
    public Integer getTimeBank() {
        return timeBank;
    }

    @Override
    public Integer increaseTimeBank(Integer amount) {
        timeBank += amount;
        return timeBank;
    }

    @Override
    public Integer getTotalTime() {
        return playerTime + timeBank;
    }

    @Override
    public Integer reduceSecond(boolean timeBankAllowed) {
        if (playerTime > 0) {
            playerTime--;
            return playerTime;
        }
        if (timeBankAllowed) {
            timeBank--;
            return timeBank;
        }
        return 0;
    }

    @Override
    public Integer getPlayerTime() {
        return playerTime;
    }

    @Override
    public void initPlayerTime(Integer time) {
    	  this.playerTime = time;
    }
}
