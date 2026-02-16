package org.moper.cap.property.exception;

import org.moper.cap.core.exception.CapFrameworkRuntimeException;

public class PropertyException extends CapFrameworkRuntimeException {
    public PropertyException(String message) {
        super(message);
    }

    public PropertyException(String message, Throwable cause) {
        super(message, cause);
    }
}
