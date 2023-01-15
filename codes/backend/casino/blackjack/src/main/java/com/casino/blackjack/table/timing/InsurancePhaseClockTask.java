package com.casino.blackjack.table.timing;

import java.util.TimerTask;
import java.util.logging.Logger;

import com.casino.blackjack.table.BlackjackTable;
import com.casino.common.table.Thresholds;

public class InsurancePhaseClockTask extends TimerTask {
	private BlackjackTable table;
	private static final Logger LOGGER = Logger.getLogger(InsurancePhaseClockTask.class.getName());

	public InsurancePhaseClockTask(BlackjackTable table) {
		this.table = table;
		Thresholds thresholds = table.getTableCard().getThresholds();
		table.updateCounterTime(thresholds.secondPhaseTime());
	}

	@Override
	public void run() {
		if (!table.isClockTicking()) {
			return;
		}
		int curTime = table.getCounterTime();
		curTime--;
		table.updateCounterTime(curTime);
		LOGGER.fine("InsurancePhase running, left:" + curTime);
		if (curTime == 0) {
			table.stopClock();
			table.onInsurancePhaseEnd();
		}
	}
}
