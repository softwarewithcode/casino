package com.casino.common.table;

import java.util.UUID;

import com.casino.common.game.Game;
import com.casino.common.language.Language;
import com.casino.common.game.phase.PhasePath;
import com.casino.common.table.structure.TableType;

public record TableData(
		PhasePath phases,
		TableStatus initialStatus,
		TableThresholds thresholds, 
		UUID id, 
		Language language, 
		TableType tableType,
		Game game) {

}
