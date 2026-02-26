package org.moper.cap.property.exception;

/**
 * 属性冲突异常 </br>
 *
 * 当尝试注册一个已经存在的属性时抛出此异常
 */
public class PropertyConflictException extends PropertyException {
    public PropertyConflictException(String message) {
        super(message);
    }

    public PropertyConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
