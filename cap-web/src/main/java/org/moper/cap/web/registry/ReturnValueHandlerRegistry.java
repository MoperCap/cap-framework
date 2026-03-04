package org.moper.cap.web.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.annotation.Priority;
import org.moper.cap.web.handler.*;
import org.moper.cap.web.model.HandlerMapping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * 返回值处理器注册表。
 *
 * <p>管理所有 {@link ReturnValueHandler} 实现，按优先级（降序）匹配并处理控制器方法的返回值。
 * 使用 {@link ServiceLoader} 加载所有实现，通过 {@link Priority} 注解获取优先级。
 */
@Slf4j
public class ReturnValueHandlerRegistry {

    private final ObjectMapper objectMapper;
    private volatile List<ReturnValueHandler> handlers;

    /**
     * 使用 ServiceLoader 加载并按优先级排序的方式初始化注册表。
     *
     * @param objectMapper Jackson ObjectMapper，用于 JSON 序列化
     */
    public ReturnValueHandlerRegistry(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.handlers = loadAndSort();
    }

    /**
     * 注册自定义返回值处理器（添加至列表并重新排序）。
     *
     * @param handler 处理器实现
     */
    public void addHandler(ReturnValueHandler handler) {
        handlers.add(handler);
        handlers.sort(Comparator.comparingInt(this::getPriority).reversed());
    }

    /**
     * 清除缓存，重新从 ServiceLoader 加载并排序处理器。
     */
    public void clearCache() {
        this.handlers = loadAndSort();
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

    private List<ReturnValueHandler> loadAndSort() {
        List<ReturnValueHandler> list = new ArrayList<>();
        ServiceLoader.load(ReturnValueHandler.class).forEach(handler -> {
            injectDependencies(handler);
            list.add(handler);
            log.debug("加载返回值处理器: {} (priority={})", handler.getClass().getName(), getPriority(handler));
        });
        list.sort(Comparator.comparingInt(this::getPriority).reversed());
        log.info("ReturnValueHandlerRegistry 共加载 {} 个返回值处理器", list.size());
        return list;
    }

    private void injectDependencies(ReturnValueHandler handler) {
        if (handler instanceof ResponseEntityHandler h) {
            h.setObjectMapper(objectMapper);
        } else if (handler instanceof JsonReturnValueHandler h) {
            h.setObjectMapper(objectMapper);
        }
    }

    private int getPriority(ReturnValueHandler handler) {
        Priority priority = handler.getClass().getAnnotation(Priority.class);
        return priority != null ? priority.value() : 0;
    }
}
