package com.github.vanbv.num.exception;

public class NumException extends RuntimeException {

    public NumException(String message) {
        super(message);
    }

    public NumException(Throwable cause) {
        super(cause);
    }

    public NumException(String message, Throwable cause) {
        super(message, cause);
    }
}
