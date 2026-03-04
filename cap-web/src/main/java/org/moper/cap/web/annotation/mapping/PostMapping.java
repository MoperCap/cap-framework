package org.moper.cap.web.annotation.mapping;

import java.lang.annotation.*;

/**
 * 将 HTTP POST 请求映射到控制器方法的快捷注解
 *
 * <p>等同于 {@code @RequestMapping(method = HttpMethod.POST)}。
 *
 * <p>使用示例：
 * <pre>
 * {@code
 * @PostMapping(path = "/users", consumes = "application/json")
 * public User createUser(@RequestBody User user) { ... }
 * }
 * </pre>
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping
public @interface PostMapping {

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
