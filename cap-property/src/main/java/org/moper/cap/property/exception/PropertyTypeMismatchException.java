package org.moper.cap.property.exception;

/**
 * 属性类型转换异常
 */
public class PropertyTypeMismatchException extends PropertyException {
    public PropertyTypeMismatchException(String message) {
        super(message);
    }

    public PropertyTypeMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
