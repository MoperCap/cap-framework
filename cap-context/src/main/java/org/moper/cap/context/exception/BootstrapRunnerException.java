package org.moper.cap.context.exception;

public class BootstrapRunnerException extends RuntimeException {
    public BootstrapRunnerException(String message) {
        super(message);
    }

    public BootstrapRunnerException(String message, Throwable cause) {
        super(message, cause);
    }
}
