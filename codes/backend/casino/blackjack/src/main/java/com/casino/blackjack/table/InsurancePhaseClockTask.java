package com.casino.blackjack.table;

import java.util.TimerTask;

public class InsurancePhaseClockTask extends TimerTask {
	private BlackjackTable table;
	private int secondsLeft;

	public InsurancePhaseClockTask(BlackjackTable table) {
		System.out.println("InsurancePhaseTask:" + table.getId());
		this.table = table;
		secondsLeft = table.getInsuranceInfo().insuranceRoundTime();
	}

	@Override
	public void run() {
		if (!table.getClock().isTicking()) {
			return;
		}
		secondsLeft--;
		System.out.println("InsurancePhaseTask time left:" + secondsLeft + " table:" + table.getId());
		if (secondsLeft == 0) {
			table.onInsurancePhaseEnd();
			table.getClock().stopClock();
		}
	}
}
