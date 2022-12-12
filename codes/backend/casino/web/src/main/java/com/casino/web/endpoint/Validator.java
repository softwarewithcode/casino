package com.casino.web.endpoint;

import java.util.UUID;

public class Validator {

	public static UUID validateUUID(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id missing");
		}
		UUID tableId;
		tableId = UUID.fromString(id);
		if (tableId.toString().equals(id))
			return tableId;
		else
			throw new IllegalArgumentException("not a valid id " + id);
	}
}
