package org.moper.cap.annotation;

import java.lang.annotation.*;

/**
 * 从 Environment 中注入属性值 </br>
 * 支持格式：{@code ${key:defaultValue}}
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Value {

    /**
     * 属性表达式，格式为 {@code ${key}} 或 {@code ${key:defaultValue}}
     */
    String value();
}
