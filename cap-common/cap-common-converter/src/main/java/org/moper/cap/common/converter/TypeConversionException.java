package org.moper.cap.common.converter;

/**
 * 类型转换异常
 */
public class TypeConversionException extends RuntimeException {

    public TypeConversionException(String message) {
        super(message);
    }

    public TypeConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
