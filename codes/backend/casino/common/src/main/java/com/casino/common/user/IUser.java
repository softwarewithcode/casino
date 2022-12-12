package com.casino.common.user;

import java.math.BigDecimal;
import java.util.UUID;

public interface IUser {

	public String getName();

	public UUID getId();

	public Status getStatus();

	public BigDecimal getBalance();

//	public List<UUID> getTables();
}
