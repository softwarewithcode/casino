package com.casino.common.exception;

public class IllegalSplitException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final Integer code;

	public IllegalSplitException(String message, int code) {
		super(message);
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
}
