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
	private int secondsLeft;

	public BetPhaseClockTask(ICasinoTable table) {
		this.table = table;
		Thresholds thresholds = table.getThresholds();
		secondsLeft = thresholds.betPhaseTime();
		System.out.println("BET PHASE CLOCK TASK NEW");
	}

	@Override
	public void run() {
		if (!table.isClockTicking()) {
			LOGGER.fine("BetPhaseClockTask, clock not ticking:" + secondsLeft);
			return;
		}
		if (shouldPrepareNewRound()) {
			LOGGER.fine("BetPhaseClockTask, clear previous round" + table);
			table.prepareNewRound();
		}
		secondsLeft--;
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("BetPhaseClockTask, secondsLeft:" + secondsLeft + " in table:" + table.getId());
		LOGGER.info("BetPhaseClock running, left:" + secondsLeft);
		if (secondsLeft == 0) {
			table.stopClock();
			table.onBetPhaseEnd();
		}
	}

	private boolean shouldPrepareNewRound() {
		return secondsLeft == table.getThresholds().betPhaseTime() && table.getGamePhase() == GamePhase.ROUND_COMPLETED;
	}

}