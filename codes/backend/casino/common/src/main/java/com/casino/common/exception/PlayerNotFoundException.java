package com.casino.common.exception;

public class PlayerNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private Integer code;

	public PlayerNotFoundException() {
		// for method expression
	}

	public PlayerNotFoundException(String message, int code) {
		super(message);
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
