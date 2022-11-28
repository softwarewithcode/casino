package com.casino.common.bet;

public class BetInfo {
	private final BetValues betValues;
	private int betRoundTimeLeft;

	public BetInfo(BetValues values) {
		super();
		this.betValues = values;
		this.betRoundTimeLeft = betValues.betRoundTime();
	}

	public int getBetRoundTimeLeft() {
		return betRoundTimeLeft;
	}

	public void setBetRoundTimeLeft(int betRoundTimeLeft) {
		this.betRoundTimeLeft = betRoundTimeLeft;
	}

	public BetValues getBetValues() {
		return betValues;
	}

}
