package org.moper.cap.web.annotation.mapping;

import java.lang.annotation.*;

/**
 * 将 HTTP PATCH 请求映射到处理方法的快捷注解。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PatchMapping {

    String value() default "";
}
