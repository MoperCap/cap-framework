package org.moper.cap.web.annotation;

import java.lang.annotation.*;

/**
 * 将 HTTP 请求体反序列化并绑定到方法参数
 *
 * <p>使用示例：
 * <pre>
 * {@code
 * @PostMapping("/users")
 * public User createUser(@RequestBody User user) { ... }
 * }
 * </pre>
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestBody {

    /**
     * 请求体是否必须存在（默认为 {@code true}）
     */
    boolean required() default true;
}
