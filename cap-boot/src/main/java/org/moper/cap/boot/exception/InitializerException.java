package org.moper.cap.boot.exception;

import org.moper.cap.core.exception.CapFrameworkRuntimeException;

public class InitializerException extends CapFrameworkRuntimeException {
    public InitializerException(String message) {
        super(message);
    }

    public InitializerException(String message, Throwable cause) {
        super(message, cause);
    }
}
