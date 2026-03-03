package org.moper.cap.web.annotation;

import java.lang.annotation.*;

/**
 * 标注一个类为 Web MVC 控制器
 *
 * <p>可与 {@code @Capper} 配合使用以进行自定义 Bean 配置：
 * <pre>
 * {@code
 * @Controller
 * @Capper(scope = BeanScope.PROTOTYPE)
 * public class UserController {
 *
 *     @GetMapping("/users")
 *     public String listUsers() { ... }
 * }
 * }
 * </pre>
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {
}
