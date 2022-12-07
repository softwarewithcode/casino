package com.casino.common.bet;

import java.util.TimerTask;

import com.casino.common.table.ICasinoTable;

public class BetPhaseClockTask extends TimerTask {
	private ICasinoTable table;
	private int secondsLeft;

	public BetPhaseClockTask(ICasinoTable table) {
		this.table = table;
		Thresholds thresholds = table.getThresholds();
		secondsLeft = thresholds.betPhaseTime();
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