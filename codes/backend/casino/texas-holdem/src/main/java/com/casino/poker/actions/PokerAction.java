package com.casino.poker.actions;

import com.casino.common.bet.Range;
import com.casino.common.action.PlayerAction;

public class PokerAction implements PlayerAction {

	private final Range range;
	private final PokerActionType type;

	private PokerAction(Range range, PokerActionType action) {
		this.range = range;
		this.type = action;
	}

	public Range getRange() {
		return range;
	}

	public PokerActionType getType() {
		return type;
	}

	public static PokerAction of(Range range, PokerActionType action) {
		return new PokerAction(range, action);
	}

	@Override
	public String toString() {
		return "PokerAction [" + (type != null ? "type=" + type : "") + "]";
	}

}
