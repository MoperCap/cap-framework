package org.moper.cap.web.annotation.request;

import java.lang.annotation.*;

/**
 * 将方法参数绑定到 HTTP Cookie 值
 *
 * <p>使用示例：
 * <pre>
 * {@code
 * @GetMapping("/profile")
 * public Profile getProfile(@CookieValue("sessionId") String sessionId) { ... }
 *
 * // 可选 Cookie，带默认值
 * @GetMapping("/profile")
 * public Profile getProfile(@CookieValue(value = "theme", required = false, defaultValue = "light") String theme) { ... }
 * }
 * </pre>
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CookieValue {

    /**
     * Cookie 名称（{@code name} 的别名）
     */
    String value() default "";

    /**
     * Cookie 名称
     */
    String name() default "";

    /**
     * 是否必须存在该 Cookie（默认为 {@code true}）
     */
    boolean required() default true;

    /**
     * Cookie 不存在时使用的默认值
     */
    String defaultValue() default "";
}
