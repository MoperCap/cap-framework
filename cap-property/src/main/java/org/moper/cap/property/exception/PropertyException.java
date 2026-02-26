package org.moper.cap.property.exception;

/**
 * 属性异常基类
 */
public class PropertyException extends RuntimeException {
    public PropertyException(String message) {
        super(message);
    }

    public PropertyException(String message, Throwable cause) {
        super(message, cause);
    }
}
