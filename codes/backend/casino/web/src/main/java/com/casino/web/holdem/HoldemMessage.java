package com.casino.web.holdem;

import com.casino.common.game.Game;
import com.casino.poker.export.TexasHoldemCashGamePlayerAction;
import com.casino.web.common.ClientMessage;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIncludeProperties(value = { "game", "action", "amount" })
public final class HoldemMessage extends ClientMessage {
	private TexasHoldemCashGamePlayerAction action;

	@Override
	public Game getGame() {
		return Game.TEXAS_HOLDEM;
	}

	public TexasHoldemCashGamePlayerAction getAction() {
		return action;
	}

	public void setAction(TexasHoldemCashGamePlayerAction action) {
		this.action = action;
	}

}
