package com.casino.common.game.phase;

import com.casino.common.table.structure.ICasinoTable;

public interface GamePhaser {
    void prepareNextGamePhase();
    ICasinoTable getTable();
}
