package org.moper.cap.web.annotation.response;

import java.lang.annotation.*;

/**
 * 标注控制器方法的返回值应直接写入 HTTP 响应体（而非视图解析）
 *
 * <p>当标注在类上时，对类中所有方法生效。
 *
 * <p>使用示例：
 * <pre>
 * {@code
 * @Controller
 * public class UserController {
 *
 *     @ResponseBody
 *     @GetMapping("/api/users")
 *     public List<User> getUsers() { ... }
 * }
 * }
 * </pre>
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ResponseBody {
}
