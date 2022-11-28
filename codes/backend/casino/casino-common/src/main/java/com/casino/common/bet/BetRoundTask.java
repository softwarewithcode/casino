package com.casino.common.bet;

import java.util.TimerTask;

import com.casino.common.table.ICasinoTable;

public class BetRoundTask extends TimerTask {
	private ICasinoTable table;

	public BetRoundTask(ICasinoTable table) {
		this.table = table;
	}

	@Override
	public void run() {
		BetInfo info = table.getBetInfo();
		int seconds = info.getBetRoundTimeLeft();
		seconds--;
		System.out.println("Time:" + seconds);
		info.setBetRoundTimeLeft(seconds);
		if (seconds <= 0) {
			table.onBetRoundEnd();
		}
	}
}