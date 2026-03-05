package org.moper.cap.web.annotation.route;

import java.lang.annotation.*;

/**
 * 将 HTTP CONNECT 请求映射到控制器方法的快捷注解
 *
 * <p>使用示例：
 * <pre>
 * {@code
 * @ConnectMapping(path = "/tunnel")
 * public void connect() { ... }
 * }
 * </pre>
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConnectMapping {

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
