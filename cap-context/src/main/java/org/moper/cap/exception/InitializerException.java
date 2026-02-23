package org.moper.cap.exception;

public class InitializerException extends RuntimeException {
    public InitializerException(String message) {
        super(message);
    }

    public InitializerException(String message, Throwable cause) {
        super(message, cause);
    }
}
