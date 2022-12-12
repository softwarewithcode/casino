package com.casino.common.table;

import java.math.BigDecimal;
import java.util.UUID;

public interface IBlackjackTable {
	public boolean join(UUID playerId, String playerName, BigDecimal balance, int seatNumber);

	public void bet(UUID playerId, BigDecimal bet);

	public void split(UUID playerId);

	public void doubleDown(UUID playerId);

	public void hit(UUID playerId);

	public void stand(UUID playerId);

	public void insure(UUID playerId);

}
