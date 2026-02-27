package org.moper.cap.bean.annotation;

import java.lang.annotation.*;

/**
 * 标注需要自动注入的字段、构造函数或方法
 */
@Documented
@Target({ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Autowired {

    /**
     * 是否必须注入，默认为 true
     */
    boolean required() default true;
}
