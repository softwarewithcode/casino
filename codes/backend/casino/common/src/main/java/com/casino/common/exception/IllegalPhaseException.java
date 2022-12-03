package com.casino.common.exception;

import com.casino.common.table.phase.GamePhase;

public class IllegalPhaseException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final GamePhase currentPhase;
	private final GamePhase expected;

	public IllegalPhaseException(String message, GamePhase currentPhase, GamePhase expected) {
		super(message);
		this.currentPhase = currentPhase;
		this.expected = expected;
	}

	public GamePhase getCurrentPhase() {
		return currentPhase;
	}

	public GamePhase getExpected() {
		return expected;
	}

	@Override
	public String toString() {
		return "IllegalPhaseException [currentPhase=" + currentPhase + ", expected=" + expected + "]";
	}

}
