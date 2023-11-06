package com.casino.common.game.phase;

import com.casino.common.table.structure.CasinoTable;

public interface GamePhaser {
    void notifyPhaseCompleted();
    CasinoTable getTable();
}
