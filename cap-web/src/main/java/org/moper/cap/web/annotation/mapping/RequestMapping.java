package org.moper.cap.web.annotation.mapping;

import org.moper.cap.web.http.HttpMethod;

import java.lang.annotation.*;

/**
 * 通用请求映射注解，将 HTTP 请求映射到控制器方法
 *
 * <p>使用示例：
 * <pre>
 * {@code
 * @RequestMapping(path = "/users", method = HttpMethod.GET, produces = "application/json")
 * public List<User> getUsers() { ... }
 * }
 * </pre>
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {

    /**
     * 请求路径（{@code path} 的别名）
     */
    String[] value() default {};

    /**
     * 请求路径
     */
    String[] path() default {};

    /**
     * 限定的 HTTP 方法列表（为空时匹配所有方法）
     */
    HttpMethod[] method() default {};

    /**
     * 响应的媒体类型（如 {@code "application/json"}）
     */
    String[] produces() default {};

    /**
     * 请求接受的媒体类型（如 {@code "application/json"}）
     */
    String[] consumes() default {};
}
