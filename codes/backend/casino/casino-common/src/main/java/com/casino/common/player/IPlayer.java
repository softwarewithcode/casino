package com.casino.common.player;

import java.math.BigDecimal;
import java.util.UUID;

public interface IPlayer {
	public String getName();

	public BigDecimal getInitialBalance();

	public BigDecimal getEndBalance();

	public UUID getId();

	public void onLeave();

}
