package com.casino.common.common;

public class PlayerNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final Integer code;

	public PlayerNotFoundException(String message, int code) {
		super(message);
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
