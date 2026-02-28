package org.moper.cap.core.exception;

public class AmbiguousActiveProfileException extends RuntimeException {
    public AmbiguousActiveProfileException(String message) {
        super(message);
    }

    public AmbiguousActiveProfileException(String message, Throwable cause) {
        super(message, cause);
    }
}
