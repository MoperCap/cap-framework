package org.moper.cap.property.exception;

/**
 * 属性校验（如不合法、空值、不支持的类型等）异常 </br>
 */
public class PropertyValidationException extends PropertyException {
    public PropertyValidationException(String key, String reason) {
        super("Property validation failed for key='" + key + "': " + reason);
    }
}
