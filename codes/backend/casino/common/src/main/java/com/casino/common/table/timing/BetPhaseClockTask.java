package com.casino.common.table.timing;

import java.util.TimerTask;

import com.casino.common.table.ICasinoTable;
import com.casino.common.table.Thresholds;

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
		if (!table.isClockTicking()) {
			return;
		}
		secondsLeft--;
		System.out.println("BetPhase left:" + secondsLeft);
		if (secondsLeft == 0) {
			table.stopClock();
			table.onBetPhaseEnd();
		}
	}
}