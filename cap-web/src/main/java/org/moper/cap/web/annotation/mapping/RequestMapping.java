package org.moper.cap.web.annotation.mapping;

import org.moper.cap.web.http.HttpMethod;

import java.lang.annotation.*;

/**
 * 将请求路径映射到控制器类或方法。
 *
 * <p>标注在类上时作为基础路径前缀，与方法级别的路由注解组合使用。
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {

    String value() default "";

    HttpMethod[] method() default {};
}
