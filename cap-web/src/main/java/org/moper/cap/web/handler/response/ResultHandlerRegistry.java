package org.moper.cap.web.handler.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.priority.PriorityUtils;
import org.moper.cap.web.handler.HandlerMapping;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 返回值处理器注册表（SPI 模式）。
 *
 * <p>使用 {@link ServiceLoader} 自动发现所有 {@link ResultHandler} 实现，
 * 通过 {@link PriorityQueue} 按优先级排序，并使用 {@link ConcurrentHashMap} 缓存已匹配的处理器。
 */
@Slf4j
public class ResultHandlerRegistry {

    private final Queue<ResultHandler> handlers;
    private final Map<Class<?>, ResultHandler> cache;

    /**
     * 使用 ServiceLoader 加载并按优先级排序的方式初始化注册表。
     *
     * @param objectMapper Jackson ObjectMapper，用于 JSON 序列化
     */
    public ResultHandlerRegistry(ObjectMapper objectMapper) {
        this.handlers = new PriorityQueue<>(
                Comparator.comparingInt(h -> PriorityUtils.getPriority(h.getClass()))
        );
        this.cache = new ConcurrentHashMap<>();

        ServiceLoader<ResultHandler> loader = ServiceLoader.load(ResultHandler.class);
        for (ResultHandler handler : loader) {
            injectDependencies(handler, objectMapper);
            handlers.offer(handler);

            int priority = PriorityUtils.getPriority(handler.getClass());
            log.debug("注册返回值处理器: {} (priority={})", handler.getClass().getName(), priority);
        }

        log.info("ResultHandlerRegistry 共注册 {} 个返回值处理器", handlers.size());
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
        ResultHandler cached = cache.get(returnType);
        if (cached != null) {
            cached.handle(returnValue, mapping, request, response);
            return;
        }

        for (ResultHandler handler : handlers) {
            if (handler.supports(returnType, mapping)) {
                cache.put(returnType, handler);
                handler.handle(returnValue, mapping, request, response);
                return;
            }
        }

        throw new IllegalStateException(
                "No suitable ResultHandler found for return type: "
                        + (returnType != null ? returnType.getName() : "null"));
    }

    private void injectDependencies(ResultHandler handler, ObjectMapper objectMapper) {
        try {
            Method method = handler.getClass().getMethod("setObjectMapper", ObjectMapper.class);
            method.invoke(handler, objectMapper);
        } catch (Exception ignored) {
        }
    }
}
