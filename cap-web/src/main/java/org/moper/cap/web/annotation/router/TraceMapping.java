package org.moper.cap.web.annotation.router;

import java.lang.annotation.*;

/**
 * 将 HTTP TRACE 请求映射到控制器方法的快捷注解
 *
 * <p>使用示例：
 * <pre>
 * {@code
 * @TraceMapping(path = "/trace")
 * public void trace() { ... }
 * }
 * </pre>
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TraceMapping {

    /**
     * 请求路径（{@code path} 的别名）
     */
    String[] value() default {};

    /**
     * 请求路径
     */
    String[] path() default {};

    /**
     * 响应的媒体类型（如 {@code "application/json"}）
     */
    String[] produces() default {};

    /**
     * 请求接受的媒体类型（如 {@code "application/json"}）
     */
    String[] consumes() default {};
}
