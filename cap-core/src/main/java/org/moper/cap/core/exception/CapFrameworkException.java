package org.moper.cap.core.exception;

public class CapFrameworkException extends Exception {
    public CapFrameworkException(String message) {
        super(message);
    }

    public CapFrameworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
