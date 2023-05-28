package com.casino.poker.round;

import com.casino.poker.table.HoldemTable;

import java.util.TimerTask;

public final class NewRoundTask extends TimerTask {
    private final HoldemTable table;

    public NewRoundTask(HoldemTable table) {
        this.table = table;
    }

    @Override
    public void run() {
        table.getDealer().onRoundStart();
    }
}
