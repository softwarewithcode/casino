package com.casino.web.common;

import java.math.BigDecimal;

import com.casino.common.action.PlayerAction;
import com.casino.common.game.Game;


public interface Message {
	PlayerAction getAction();

	BigDecimal getAmount();
	
	Game getGame();
}
