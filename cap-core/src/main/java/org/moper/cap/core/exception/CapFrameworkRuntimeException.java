package org.moper.cap.core.exception;

public class CapFrameworkRuntimeException extends CapFrameworkException {
    public CapFrameworkRuntimeException(String message) {
        super(message);
    }

    public CapFrameworkRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
