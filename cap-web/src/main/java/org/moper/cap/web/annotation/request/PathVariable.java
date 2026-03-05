package org.moper.cap.web.annotation.request;

import java.lang.annotation.*;

/**
 * 将方法参数绑定到 URI 路径变量
 *
 * <p>使用示例：
 * <pre>
 * {@code
 * @GetMapping("/users/{id}")
 * public User getUser(@PathVariable("id") Long userId) { ... }
 *
 * // 当参数名与路径变量名一致时，可省略 value
 * @GetMapping("/users/{id}")
 * public User getUser(@PathVariable Long id) { ... }
 * }
 * </pre>
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface PathVariable {

    /**
     * 路径变量名称（{@code name} 的别名）
     */
    String value() default "";

    /**
     * 路径变量名称
     */
    String name() default "";

    /**
     * 是否必须存在该路径变量（默认为 {@code true}）
     */
    boolean required() default true;
}
