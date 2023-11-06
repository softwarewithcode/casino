package com.casino.common.game.phase.insurance;

import com.casino.common.table.timing.TableClockHandler;

//Related to games where players can insure at the same time within timelimits
public interface InsurancePhaser extends TableClockHandler {
    void onInsurancePhaseEnd();

    Integer getInsurancePhaseTime();
}
