package com.casino.common.exception;

import java.io.Serial;

public class IllegalPlayerActionException extends RuntimeException {
	@Serial
	private static final long serialVersionUID = 1L;

	public IllegalPlayerActionException(String message) {
		super(message);
	}

}
