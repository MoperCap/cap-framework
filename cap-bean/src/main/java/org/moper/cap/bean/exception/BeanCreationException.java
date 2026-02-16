package org.moper.cap.bean.exception;

public class BeanCreationException extends BeanException {
    public BeanCreationException(String message) {
        super(message);
    }

    public BeanCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
