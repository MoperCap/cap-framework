package org.moper.cap.property.exception;

public class PropertyCastException extends PropertyException {
    public PropertyCastException(String message) {
        super(message);
    }

    public PropertyCastException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
