package com.casino.common.bet;

public class BetInfo {
	private final BetValues betData;
	private int betTimeLeft;

	public BetInfo(BetValues betData, int betTimeLeft) {
		super();
		this.betData = betData;
		this.betTimeLeft = betTimeLeft;
	}

	public int getBetTimeLeft() {
		return betTimeLeft;
	}

	public void setBetTimeLeft(int betTimeLeft) {
		this.betTimeLeft = betTimeLeft;
	}

	public BetValues getBetData() {
		return betData;
	}

}
