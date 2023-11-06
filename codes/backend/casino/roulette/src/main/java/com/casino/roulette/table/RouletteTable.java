package com.casino.roulette.table;

public interface RouletteTable {
	RouletteWheel getWheel();
    void prepareForNextRound();
}
