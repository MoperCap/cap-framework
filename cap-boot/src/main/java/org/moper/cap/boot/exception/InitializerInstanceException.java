package org.moper.cap.boot.exception;

public class InitializerInstanceException extends InitializerException {
    public InitializerInstanceException(String message) {
        super(message);
    }

    public InitializerInstanceException(String message, Throwable cause) {
        super(message, cause);
    }
}
