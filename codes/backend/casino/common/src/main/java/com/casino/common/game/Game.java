package com.casino.common.game;

public enum Game {
	BLACKJACK(1), TEXAS_HOLDEM(2), ROULETTE(3);

	private final int gameNumber;

	private Game(int gameNumber) {
		this.gameNumber = gameNumber;
	}

	public int getGameNumber() {
		return gameNumber;
	}

}
