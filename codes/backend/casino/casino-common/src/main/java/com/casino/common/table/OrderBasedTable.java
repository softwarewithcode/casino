package com.casino.common.table;

import java.math.BigDecimal;

/*
 * For example blackjack and red dog games are order based games. 
 * Roulette is not order based as people tend to add chips at the same time.
 */
public abstract class OrderBasedTable extends BaseTable {
	
	protected OrderBasedTable(Status initialStatus, BigDecimal minBet, BigDecimal maxBet, int minPlayers, int maxPlayers, Type type) {
		super(initialStatus, minBet, maxBet, minPlayers, maxPlayers, type);
	}

}
