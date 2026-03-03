package org.moper.cap.web.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.moper.cap.web.model.HandlerMapping;

/**
 * void 返回值处理器。
 *
 * <p>处理方法返回值为 {@code void} 或返回 {@code null} 的情形，不向响应体写入任何内容。
 */
public class VoidReturnValueHandler implements ReturnValueHandler {

    @Override
    public boolean supports(Class<?> returnType, HandlerMapping mapping) {
        return returnType == null || returnType == void.class || returnType == Void.class;
    }

    @Override
    public void handle(Object returnValue,
                       HandlerMapping mapping,
                       HttpServletRequest request,
                       HttpServletResponse response) {
        // 不写入任何响应内容，由控制器方法自行处理（如直接操作 response）
        // 默认状态码由 Servlet 容器设置为 200，此处不覆盖
    }
}
