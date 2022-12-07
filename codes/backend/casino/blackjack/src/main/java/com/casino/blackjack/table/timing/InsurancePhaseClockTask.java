package com.casino.blackjack.table.timing;

import java.util.TimerTask;

import com.casino.blackjack.table.BlackjackTable;

public class InsurancePhaseClockTask extends TimerTask {
	private BlackjackTable table;
	private int secondsLeft;

	public InsurancePhaseClockTask(BlackjackTable table) {
		this.table = table;
		secondsLeft = table.getThresholds().secondPhaseTime();
	}

	@Override
	public void run() {
		if (!table.getClock().isTicking()) {
			return;
		}
		secondsLeft--;
		if (secondsLeft == 0) {
			table.onInsurancePhaseEnd();
			table.getClock().stopClock();
		}
	}
}
