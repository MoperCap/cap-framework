package org.moper.cap.property.exception;

/**
 * 未找到属性异常
 */
public class NoSuchPropertyException extends PropertyException{
    public NoSuchPropertyException(String key) {
        super("Property not found: " + key);
    }
}
