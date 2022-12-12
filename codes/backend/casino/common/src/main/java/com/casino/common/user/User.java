package com.casino.common.user;

import java.util.UUID;

public class User {

	private final String name;
	private final UUID tableId;
	private final UUID id;

	public User(String name, UUID tableId, UUID id) {
		super();
		this.name = name;
		this.tableId = tableId;
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public UUID getTableId() {
		return tableId;
	}

	public UUID getId() {
		return id;
	}
}
