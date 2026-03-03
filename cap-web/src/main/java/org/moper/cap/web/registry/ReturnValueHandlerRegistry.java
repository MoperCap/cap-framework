package org.moper.cap.web.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.moper.cap.web.handler.*;
import org.moper.cap.web.model.HandlerMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * 返回值处理器注册表。
 *
 * <p>管理所有 {@link ReturnValueHandler} 实现，按优先级顺序匹配并处理控制器方法的返回值。
 */
public class ReturnValueHandlerRegistry {

    private final List<ReturnValueHandler> handlers = new ArrayList<>();

    /**
     * 使用默认处理器集初始化注册表。
     *
     * @param objectMapper Jackson ObjectMapper，用于 JSON 序列化
     */
    public ReturnValueHandlerRegistry(ObjectMapper objectMapper) {
        // 顺序很重要：ResponseEntity > String（@ResponseBody）> JSON（@ResponseBody）> Void > Null(兜底)
        handlers.add(new ResponseEntityHandler(objectMapper));
        handlers.add(new StringReturnValueHandler());
        handlers.add(new VoidReturnValueHandler());
        handlers.add(new JsonReturnValueHandler(objectMapper));
        handlers.add(new NullReturnValueHandler());
    }

    /**
     * 注册自定义返回值处理器（添加至末尾）。
     *
     * @param handler 处理器实现
     */
    public void addHandler(ReturnValueHandler handler) {
        handlers.add(handler);
    }

    /**
     * 处理控制器方法的返回值。
     *
     * @param returnValue 控制器方法的返回值（可为 null）
     * @param returnType  返回值的声明类型（可为 null 表示 void）
     * @param mapping     当前处理的路由映射
     * @param request     HTTP 请求
     * @param response    HTTP 响应
     * @throws Exception 若没有合适的处理器或处理失败
     */
    public void handle(Object returnValue,
                       Class<?> returnType,
                       HandlerMapping mapping,
                       HttpServletRequest request,
                       HttpServletResponse response) throws Exception {
        for (ReturnValueHandler handler : handlers) {
            if (handler.supports(returnType, mapping)) {
                handler.handle(returnValue, mapping, request, response);
                return;
            }
        }
        throw new IllegalStateException(
                "No suitable ReturnValueHandler found for return type: "
                        + (returnType != null ? returnType.getName() : "null"));
    }
}
