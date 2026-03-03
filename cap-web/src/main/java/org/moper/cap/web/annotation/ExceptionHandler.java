package org.moper.cap.web.annotation;

import java.lang.annotation.*;

/**
 * 标注方法为全局异常处理器，用于处理控制器中抛出的指定异常
 *
 * <p>使用示例：
 * <pre>
 * {@code
 * @Controller
 * public class GlobalExceptionController {
 *
 *     @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
 *     public String handleException(Exception ex) {
 *         return ex.getMessage();
 *     }
 * }
 * }
 * </pre>
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExceptionHandler {

    /**
     * 要处理的异常类型列表（为空时通过方法参数类型推断）
     */
    Class<? extends Throwable>[] value() default {};
}
