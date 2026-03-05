package org.moper.cap.web.view.support;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.web.view.ViewHandler;
import org.moper.cap.web.view.ViewHandlerRegistry;
import org.moper.cap.web.router.RouteDefinition;
import org.moper.cap.common.priority.PriorityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认视图处理器注册表
 *
 * 职责：
 * 1. 通过 ServiceLoader 发现所有 ViewHandler 实现
 * 2. 按优先级排序到 Queue 中
 * 3. 维护返回值类型 -> ViewHandler 的缓存
 * 4. 根据返回值类型查找对应的处理器
 */
@Slf4j
public class DefaultViewHandlerRegistry implements ViewHandlerRegistry {

    private final Queue<ViewHandler> handlers;
    private final Map<Class<?>, ViewHandler> cache;

    public DefaultViewHandlerRegistry() {
        this.handlers = new PriorityQueue<>(
                Comparator.comparingInt(h -> PriorityUtils.getPriority(h.getClass()))
        );
        this.cache = new ConcurrentHashMap<>();

        ServiceLoader<ViewHandler> loader = ServiceLoader.load(ViewHandler.class);
        for (ViewHandler handler : loader) {
            handlers.offer(handler);
            int priority = PriorityUtils.getPriority(handler.getClass());
            log.debug("注册视图处理器: {} (priority={})",
                    handler.getClass().getSimpleName(), priority);
        }

        log.info("ViewHandlerRegistry 共注册 {} 个视图处理器", handlers.size());
    }

    @Override
    public void handle(Object returnValue,
                      Class<?> returnType,
                      RouteDefinition mapping,
                      HttpServletRequest request,
                      HttpServletResponse response) throws Exception {

        if (returnType == null) {
            returnType = returnValue != null ? returnValue.getClass() : Void.TYPE;
        }

        ViewHandler cached = cache.get(returnType);
        if (cached != null) {
            log.debug("从缓存命中视图处理器: {} (returnType={})",
                    cached.getClass().getSimpleName(), returnType.getSimpleName());
            cached.handle(returnValue, mapping, request, response);
            return;
        }

        for (ViewHandler handler : handlers) {
            if (handler.supports(returnType, mapping)) {
                cache.put(returnType, handler);
                log.debug("使用视图处理器: {} (returnType={})",
                        handler.getClass().getSimpleName(), returnType.getSimpleName());
                handler.handle(returnValue, mapping, request, response);
                return;
            }
        }

        throw new IllegalStateException(
                "No suitable ViewHandler found for return type: " +
                (returnType != null ? returnType.getName() : "null"));
    }
}
