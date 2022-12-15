package com.casino.blackjack.ext;

import java.math.BigDecimal;
import java.util.UUID;

import com.casino.common.user.Bridge;

public interface BlackjackReverseProxy {
	public boolean join(Bridge user, String seatNumber);

	public boolean watch(Bridge user);

	public void bet(UUID playerId, BigDecimal bet);

	public void split(UUID playerId);

	public void doubleDown(UUID playerId);

	public void hit(UUID playerId);

	public void stand(UUID playerId);

	public void insure(UUID playerId);

}
