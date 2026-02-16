package org.moper.cap.bean.exception;

public class NoSuchBeanException extends BeanException {
    public NoSuchBeanException(String message) {
        super("No such bean: " + message);
    }

    public NoSuchBeanException(String message, Throwable cause) {
        super("No such bean: " + message, cause);
    }
}
