package org.moper.cap.web.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.moper.cap.web.model.HandlerMapping;

/**
 * 返回值处理器接口。
 *
 * <p>每个实现负责处理控制器方法返回的特定类型的值，
 * 将结果写入 HTTP 响应或执行视图跳转。
 */
public interface ReturnValueHandler {

    /**
     * 判断此处理器是否支持给定的返回值类型。
     *
     * @param returnType 返回值类型（可为 null 表示 void）
     * @param mapping    当前处理的路由映射
     * @return 如果支持则返回 {@code true}
     */
    boolean supports(Class<?> returnType, HandlerMapping mapping);

    /**
     * 处理返回值，将结果写入 HTTP 响应。
     *
     * @param returnValue 控制器方法的返回值（可为 null）
     * @param mapping     当前处理的路由映射
     * @param request     HTTP 请求
     * @param response    HTTP 响应
     * @throws Exception 处理过程中可能抛出的异常
     */
    void handle(Object returnValue,
                HandlerMapping mapping,
                HttpServletRequest request,
                HttpServletResponse response) throws Exception;
}
