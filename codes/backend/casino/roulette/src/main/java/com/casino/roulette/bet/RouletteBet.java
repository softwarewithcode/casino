package com.casino.roulette.bet;

import java.math.BigDecimal;
import java.util.UUID;

import com.casino.roulette.export.BetType;


public interface RouletteBet {

	BigDecimal getAmount();

	BigDecimal getWinAmount();
	
	Boolean success();

	Integer getScale();

	UUID getId();

	BetType getType();

	Integer getPosition();

	void complete(Integer winNumber);
	
	RouletteBet replicate();
	BetData getData();
	
	boolean isCompleted();
}
