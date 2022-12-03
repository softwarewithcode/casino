package com.casino.common.exception;

public class IllegalPlayerActionException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final Integer code;

	public IllegalPlayerActionException(String message, int code) {
		super(message);
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
