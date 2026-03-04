package org.moper.cap.web.exception;

import java.lang.reflect.Method;

/**
 * 异常处理器的元数据记录。
 *
 * <p>保存通过 {@link org.moper.cap.web.annotation.ExceptionHandler} 注解标注的方法信息，
 * 供 {@link ExceptionResolverRegistry} 在运行时匹配和调用。
 *
 * @param handler       持有该方法的控制器实例，不能为 null
 * @param method        异常处理方法，不能为 null
 * @param exceptionType 能处理的异常类型，不能为 null
 * @param order         优先级（数值越小优先级越高）
 */
public record ExceptionHandlerInfo(
        Object handler,
        Method method,
        Class<? extends Throwable> exceptionType,
        int order
) {

    public ExceptionHandlerInfo {
        if (handler == null) {
            throw new IllegalArgumentException("handler must not be null");
        }
        if (method == null) {
            throw new IllegalArgumentException("method must not be null");
        }
        if (exceptionType == null) {
            throw new IllegalArgumentException("exceptionType must not be null");
        }
    }
}
