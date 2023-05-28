package com.casino.common.game.phase.insurance;

import com.casino.common.table.timing.TimeController;

//Related to games where players can insure at the same time within timelimits
public interface InsurancePhaser extends TimeController {
	void onInsurancePhaseEnd();

	Integer getInsurancePhaseTime();
}
