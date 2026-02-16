package org.moper.cap.property.exception;

public class NoSuchPropertyDefinitionException extends PropertyException {
    public NoSuchPropertyDefinitionException(String message) {
        super(message);
    }

    public NoSuchPropertyDefinitionException(String message, Throwable cause) {
      super(message, cause);
    }
}
