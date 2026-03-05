package org.moper.cap.web.annotation.mapping;

import org.moper.cap.web.http.HttpMethod;

import java.lang.annotation.*;

/**
 * 将 HTTP HEAD 请求映射到控制器方法的快捷注解
 *
 * <p>等同于 {@code @RequestMapping(method = HttpMethod.HEAD)}。
 *
 * <p>使用示例：
 * <pre>
 * {@code
 * @HeadMapping(path = "/users/{id}")
 * public void checkUser(@PathVariable Long id) { ... }
 * }
 * </pre>
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping(method = HttpMethod.HEAD)
public @interface HeadMapping {

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
