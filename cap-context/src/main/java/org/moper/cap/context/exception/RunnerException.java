package org.moper.cap.context.exception;

public class RunnerException extends RuntimeException {
    public RunnerException(String message) {
        super(message);
    }

    public RunnerException(String message, Throwable cause) {
        super(message, cause);
    }
}
