package org.moper.cap.bean.annotation;

import java.lang.annotation.*;


/**
 * 依赖注入注解
 */
@Documented
@Target({ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {

    /**
     * 指定注入的 Bean 名称，默认为空字符串，表示根据类型进行注入
     */
    String beanName() default "";
}
