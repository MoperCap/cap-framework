package org.moper.cap.boot.annotation;

import java.lang.annotation.*;

/**
 * 限定自动注入的 Bean 名称
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Qualifier {

    /**
     * Bean 名称
     */
    String value();
}
