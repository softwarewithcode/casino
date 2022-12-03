package com.casino.common.exception;

public class IllegalPlayerAction extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final Integer code;

	public IllegalPlayerAction(String message, int code) {
		super(message);
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
