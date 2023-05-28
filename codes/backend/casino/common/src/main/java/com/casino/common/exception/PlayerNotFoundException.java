package com.casino.common.exception;

import java.io.Serial;

public class PlayerNotFoundException extends RuntimeException {
	@Serial
	private static final long serialVersionUID = 1L;

	public PlayerNotFoundException() {
		// for method expression
	}

	public PlayerNotFoundException(String message, int code) {
		super(message);
	}

}
