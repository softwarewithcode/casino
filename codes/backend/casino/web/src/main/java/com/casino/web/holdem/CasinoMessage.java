package com.casino.web.holdem;

import java.math.BigDecimal;

import com.casino.common.action.PlayerAction;

public interface CasinoMessage {
	PlayerAction getAction();

	BigDecimal getAmount();
	
	
}
