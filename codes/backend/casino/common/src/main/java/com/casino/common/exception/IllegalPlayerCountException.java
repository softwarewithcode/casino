package com.casino.common.exception;

import java.io.Serial;

public class IllegalPlayerCountException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public IllegalPlayerCountException(String message) {
        super(message);
    }
}
