package com.casino.common.user;

import java.util.List;
import java.util.UUID;

public interface User {

	public String getName();

	public UUID getId();

	public Status getStatus();

	public List<UUID> getTables();
}
