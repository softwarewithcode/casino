package com.casino.common.game.phase.insurance;

import java.util.TimerTask;
import java.util.logging.Logger;

public class InsurancePhase<T extends InsurancePhaser> extends TimerTask {
	private final T insurancePhaser;
	private static final Logger LOGGER = Logger.getLogger(InsurancePhase.class.getName());

	public InsurancePhase(T insurancePhaser) {
		this.insurancePhaser = insurancePhaser;
		insurancePhaser.updateCounterTime(insurancePhaser.getInsurancePhaseTime());
	}

	@Override
	public void run() {
		if (!insurancePhaser.isClockTicking()) {
			return;
		}
		int curTime = insurancePhaser.getCounterTime();
		curTime--;
		insurancePhaser.updateCounterTime(curTime);
		LOGGER.fine("InsurancePhase running, left:" + curTime);
		if (curTime == 0) {
			insurancePhaser.stopClock();
			insurancePhaser.onInsurancePhaseEnd();
		}
	}
}
