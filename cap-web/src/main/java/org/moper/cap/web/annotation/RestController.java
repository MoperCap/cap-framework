package org.moper.cap.web.annotation;

import java.lang.annotation.*;

/**
 * 标注一个类为 RESTful 控制器，方法返回值将自动序列化为 JSON 写入响应体
 *
 * <p>相当于同时标注 {@code @Controller} 和 {@code @ResponseBody}。
 *
 * <p>可与 {@code @Capper} 配合使用以进行自定义 Bean 配置：
 * <pre>
 * {@code
 * @RestController
 * public class UserRestController {
 *
 *     @GetMapping("/api/users")
 *     public List<User> getUsers() { ... }
 * }
 * }
 * </pre>
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RestController {
}
