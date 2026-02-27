package org.moper.cap.context.exception;

public class RuntimeRunnerException extends RunnerException {
    public RuntimeRunnerException(String message) {
        super(message);
    }

    public RuntimeRunnerException(String message, Throwable cause) {
        super(message, cause);
    }
}
