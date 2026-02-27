package org.moper.cap.bean.annotation;

import java.lang.annotation.*;


/**
 * 依赖注入注解
 */
@Documented
@Target({ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {
}
