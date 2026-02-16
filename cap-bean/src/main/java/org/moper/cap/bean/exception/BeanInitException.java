package org.moper.cap.bean.exception;

public class BeanInitException extends BeanException {
    public BeanInitException(String message) {
        super(message);
    }

    public BeanInitException(String message, Throwable cause) {
        super(message, cause);
    }
}
