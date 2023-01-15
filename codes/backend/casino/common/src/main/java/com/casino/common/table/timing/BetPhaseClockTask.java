package com.casino.common.table.timing;

import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.casino.common.table.ICasinoTable;
import com.casino.common.table.Thresholds;
import com.casino.common.table.phase.GamePhase;

public class BetPhaseClockTask extends TimerTask {
	private static final Logger LOGGER = Logger.getLogger(BetPhaseClockTask.class.getName());
	private ICasinoTable table;

	public BetPhaseClockTask(ICasinoTable table) {
		this.table = table;
		Thresholds thresholds = table.getTableCard().getThresholds();
		table.updateCounterTime(thresholds.betPhaseTime());
	}

	@Override
	public void run() {
		if (!table.isClockTicking()) {
			LOGGER.fine("BetPhaseClockTask, clock not ticking:");
			return;
		}
		if (shouldPrepareNewRound()) {
			LOGGER.info("BetPhaseClockTask, clear previous round" + table);
			table.prepareNewRound();
		}
		int counterValue = table.getCounterTime();
		counterValue--;
		table.updateCounterTime(counterValue);
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("BetPhaseClockTask, secondsLeft:" + counterValue + " in table:" + table.getId());
		if (counterValue == 0) {
			table.stopClock();
			table.onBetPhaseEnd();
		}
	}

	private boolean shouldPrepareNewRound() {
		return table.getCounterTime() == table.getTableCard().getThresholds().betPhaseTime() && table.getGamePhase() == GamePhase.ROUND_COMPLETED;
	}

}