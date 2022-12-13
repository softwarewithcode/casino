package com.casino.common.validaton;

import java.util.UUID;

public class Validator {

	public static UUID validateId(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id missing");
		}
		UUID validId;
		validId = UUID.fromString(id);
		if (validId.toString().equals(id))
			return validId;
		else
			throw new IllegalArgumentException("not a valid id " + id);
	}
}
