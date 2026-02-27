package org.moper.cap.bean.fixture;

import java.lang.annotation.*;

/** 用于按注解查找 Bean 测试的标记注解 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MarkerAnnotation {
    String value() default "";
}