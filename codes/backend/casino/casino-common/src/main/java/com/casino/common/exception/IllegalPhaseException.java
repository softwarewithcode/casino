package com.casino.common.exception;

import com.casino.common.table.phase.GamePhase;

public class IllegalPhaseException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final GamePhase fromPhase;
	private final GamePhase expected;

	public IllegalPhaseException(String message, GamePhase fromPhase, GamePhase expected) {
		super(message);
		this.fromPhase = fromPhase;
		this.expected = expected;
	}

	public GamePhase getFromPhase() {
		return fromPhase;
	}

	public GamePhase getExpected() {
		return expected;
	}

}
