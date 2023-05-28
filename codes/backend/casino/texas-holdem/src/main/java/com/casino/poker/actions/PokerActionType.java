package com.casino.poker.actions;

import com.casino.common.action.PlayerAction;
import com.casino.common.message.MessageTitle;

public enum PokerActionType implements PlayerAction, MessageTitle {
	FOLD, CHECK, BET_RAISE, CALL, ALL_IN
}
