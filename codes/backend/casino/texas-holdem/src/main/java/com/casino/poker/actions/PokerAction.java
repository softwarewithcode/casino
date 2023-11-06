package com.casino.poker.actions;

import com.casino.common.bet.BetRange;
import com.casino.common.action.PlayerAction;

public class PokerAction implements PlayerAction {

	private final BetRange range;
	private final PokerActionType type;

	private PokerAction(BetRange range, PokerActionType action) {
		this.range = range;
		this.type = action;
	}

	public BetRange getRange() {
		return range;
	}

	public PokerActionType getType() {
		return type;
	}

	public static PokerAction of(BetRange range, PokerActionType action) {
		return new PokerAction(range, action);
	}

	@Override
	public String toString() {
		return "PokerAction [" + (type != null ? "type=" + type : "") + "]";
	}

}
