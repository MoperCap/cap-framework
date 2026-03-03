package org.moper.cap.web.annotation;

import java.lang.annotation.*;

/**
 * 将方法参数绑定到 HTTP 请求头
 *
 * <p>使用示例：
 * <pre>
 * {@code
 * @GetMapping("/users")
 * public List<User> getUsers(@RequestHeader("Authorization") String token) { ... }
 *
 * // 可选请求头，带默认值
 * @GetMapping("/users")
 * public List<User> getUsers(@RequestHeader(value = "X-Locale", required = false, defaultValue = "zh-CN") String locale) { ... }
 * }
 * </pre>
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestHeader {

    /**
     * 请求头名称（{@code name} 的别名）
     */
    String value() default "";

    /**
     * 请求头名称
     */
    String name() default "";

    /**
     * 是否必须存在该请求头（默认为 {@code true}）
     */
    boolean required() default true;

    /**
     * 请求头不存在时使用的默认值
     */
    String defaultValue() default "";
}
