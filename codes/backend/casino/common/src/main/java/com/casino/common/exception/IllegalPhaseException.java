package com.casino.common.exception;


import java.io.Serial;

public class IllegalPhaseException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;
    private String currentPhase;
    private String expected;

    public IllegalPhaseException(String message, String currentPhase, String expected) {
        super(message);
        this.currentPhase = currentPhase;
        this.expected = expected;
    }

    public IllegalPhaseException(String message) {
        super(message);
    }

    @Override
    public String toString() {
        return "IllegalPhaseException [currentPhase=" + currentPhase + ", expected=" + expected + "]" + getMessage();
    }

}
