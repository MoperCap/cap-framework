package org.moper.cap.web.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.moper.cap.web.model.HandlerMapping;

/**
 * null 返回值处理器（兜底处理器）。
 *
 * <p>当返回值为 null 且没有其他处理器匹配时，返回 204 No Content。
 */
public class NullReturnValueHandler implements ReturnValueHandler {

    @Override
    public boolean supports(Class<?> returnType, HandlerMapping mapping) {
        return true;
    }

    @Override
    public void handle(Object returnValue,
                       HandlerMapping mapping,
                       HttpServletRequest request,
                       HttpServletResponse response) {
        // 兜底处理：返回值为 null 时响应 204 No Content；
        // 若返回值非 null 但没有其他处理器匹配，则不修改响应状态（由 Servlet 容器默认设置 200）
        if (returnValue == null) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }
}
