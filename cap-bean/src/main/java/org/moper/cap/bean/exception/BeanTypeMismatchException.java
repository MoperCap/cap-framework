package org.moper.cap.bean.exception;

public class BeanTypeMismatchException extends BeanException {
    public BeanTypeMismatchException(String message) {
        super(message);
    }
    public BeanTypeMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
