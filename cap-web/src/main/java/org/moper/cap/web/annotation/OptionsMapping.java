package org.moper.cap.web.annotation;

import java.lang.annotation.*;

/**
 * 将 HTTP OPTIONS 请求映射到控制器方法的快捷注解
 *
 * <p>等同于 {@code @RequestMapping(method = HttpMethod.OPTIONS)}。
 *
 * <p>使用示例：
 * <pre>
 * {@code
 * @OptionsMapping(path = "/users")
 * public void usersOptions() { ... }
 * }
 * </pre>
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping
public @interface OptionsMapping {

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
