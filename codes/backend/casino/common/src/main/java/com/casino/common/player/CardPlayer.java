package com.casino.common.player;

public interface CardPlayer extends CasinoPlayer {
	boolean hasActiveHand();

	boolean canAct();
}
