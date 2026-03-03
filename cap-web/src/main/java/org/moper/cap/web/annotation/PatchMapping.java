package org.moper.cap.web.annotation;

import java.lang.annotation.*;

/**
 * 将 HTTP PATCH 请求映射到控制器方法的快捷注解
 *
 * <p>等同于 {@code @RequestMapping(method = HttpMethod.PATCH)}。
 *
 * <p>使用示例：
 * <pre>
 * {@code
 * @PatchMapping(path = "/users/{id}", consumes = "application/json")
 * public User patchUser(@PathVariable Long id, @RequestBody Map<String, Object> updates) { ... }
 * }
 * </pre>
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping
public @interface PatchMapping {

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
