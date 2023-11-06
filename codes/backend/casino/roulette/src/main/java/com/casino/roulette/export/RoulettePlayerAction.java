package com.casino.roulette.export;

import com.casino.common.action.PlayerAction;

public enum RoulettePlayerAction implements PlayerAction {
	OPEN_TABLE, JOIN, REFRESH, BET, REMOVE_LAST_OR_ALL_BETS, REMOVE_BET_FROM_POSITION, PLAY, REPEAT_LAST, FETCH_BET_POSITIONS
}
