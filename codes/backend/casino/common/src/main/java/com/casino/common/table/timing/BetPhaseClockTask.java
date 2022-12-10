package com.casino.common.table.timing;

import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.common.table.ICasinoTable;
import com.casino.common.table.Thresholds;

public class BetPhaseClockTask extends TimerTask {
	private static final Logger LOGGER = Logger.getLogger(BetPhaseClockTask.class.getName());
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
//		try {
//			if (shouldInitBetPhase())
//				table.restartBetPhase();
//		} catch (Exception e) {
//			LOGGER.log(Level.SEVERE, " error starting betPhase, cannot continue ", e);
//			table.stopClock();
//			throw e;
//		}
		secondsLeft--;
		if (secondsLeft == 0) {
			table.stopClock();
			table.onBetPhaseEnd();
		}
	}

	private boolean shouldInitBetPhase() {
		return secondsLeft == table.getThresholds().betPhaseTime();
	}

}