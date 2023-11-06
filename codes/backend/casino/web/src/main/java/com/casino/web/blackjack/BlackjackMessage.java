package com.casino.web.blackjack;

import com.casino.blackjack.export.BlackjackPlayerAction;
import com.casino.common.game.Game;
import com.casino.web.common.ClientMessage;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIncludeProperties(value = { "game", "action", "amount" })
public class BlackjackMessage extends ClientMessage {
	private BlackjackPlayerAction action;

	public BlackjackPlayerAction getAction() {
		return action;
	}

	public void setAction(BlackjackPlayerAction action) {
		this.action = action;
	}

	@Override
	public Game getGame() {
		return Game.BLACKJACK;
	}

}
