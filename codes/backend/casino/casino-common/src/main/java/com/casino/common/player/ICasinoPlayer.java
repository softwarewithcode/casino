package com.casino.common.player;

import java.math.BigDecimal;
import java.util.UUID;

public interface ICasinoPlayer {
	public String getName();

	public BigDecimal getInitialBalance();

	public BigDecimal getEndBalance();

	public UUID getId();

	public void onLeave();

	public Status getStatus();

	public void setStatus(Status status);

}
