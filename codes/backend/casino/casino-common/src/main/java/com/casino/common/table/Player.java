package com.casino.common.table;

import java.math.BigDecimal;
import java.util.UUID;

public interface Player {
	public String getName();

	public BigDecimal getInitialBalance();

	public BigDecimal getLeaveBalance();

	public UUID getId();

	public void onLeave();

}
