package org.moper.cap.boot.exception;

public class InitializerContextException extends InitializerException {
    public InitializerContextException(String message) {
        super(message);
    }
    public InitializerContextException(String message, Throwable cause) {
        super(message, cause);
    }
}
