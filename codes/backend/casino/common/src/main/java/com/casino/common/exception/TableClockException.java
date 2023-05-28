package com.casino.common.exception;

import java.io.Serial;

public class TableClockException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public TableClockException(String message) {
        super(message);
    }
}