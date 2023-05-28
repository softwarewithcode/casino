package com.casino.common.exception;


import java.io.Serial;

public class IllegalPhaseException extends RuntimeException {
	@Serial
	private static final long serialVersionUID = 1L;
	private final String currentPhase;
	private final String expected;

	public IllegalPhaseException(String message, String currentPhase, String expected) {
		super(message);
		this.currentPhase = currentPhase;
		this.expected = expected;
	}


	@Override
	public String toString() {
		return "IllegalPhaseException [currentPhase=" + currentPhase + ", expected=" + expected + "]";
	}

}
