package com.casino.common.bet;

import java.util.TimerTask;

import com.casino.common.table.ICasinoTable;

public class BetPhaseClockTask extends TimerTask {
	private ICasinoTable table;
	private int secondsLeft;

	public BetPhaseClockTask(ICasinoTable table) {
		this.table = table;
		BetThresholds thresholds = table.getBetValues();
		secondsLeft = thresholds.betRoundTime();
	}

	@Override
	public void run() {
		if (!table.getClock().isTicking()) {
			return;
		}
		secondsLeft--;
		if (secondsLeft == 0) {
			table.getClock().stopClock();
			table.onBetPhaseEnd();
		}
	}
}