package org.moper.cap.web.annotation;

import java.lang.annotation.*;

/**
 * 将方法参数绑定到 HTTP 请求参数（查询字符串或表单数据）
 *
 * <p>使用示例：
 * <pre>
 * {@code
 * @GetMapping("/users")
 * public List<User> getUsers(@RequestParam("page") int page,
 *                            @RequestParam(value = "size", required = false, defaultValue = "10") int size) { ... }
 * }
 * </pre>
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParam {

    /**
     * 请求参数名称（{@code name} 的别名）
     */
    String value() default "";

    /**
     * 请求参数名称
     */
    String name() default "";

    /**
     * 是否必须存在该参数（默认为 {@code true}）
     */
    boolean required() default true;

    /**
     * 参数不存在时使用的默认值
     */
    String defaultValue() default "";
}
