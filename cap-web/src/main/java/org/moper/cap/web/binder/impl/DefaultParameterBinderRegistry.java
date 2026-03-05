package org.moper.cap.web.binder.impl;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.web.binder.ParameterBinder;
import org.moper.cap.web.binder.ParameterBinderRegistry;
import org.moper.cap.web.binder.ParameterMetadata;
import org.moper.cap.common.priority.PriorityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DefaultParameterBinderRegistry implements ParameterBinderRegistry {

    private final Queue<ParameterBinder> binders;
    private final Map<Parameter, ParameterBinder> cache;

    public DefaultParameterBinderRegistry() {
        this.binders = new PriorityQueue<>(
            (b1, b2) -> Integer.compare(
                PriorityUtils.getPriority(b1.getClass()),
                PriorityUtils.getPriority(b2.getClass())
            )
        );
        this.cache = new ConcurrentHashMap<>();

        ServiceLoader<ParameterBinder> loader = ServiceLoader.load(ParameterBinder.class);
        for (ParameterBinder binder : loader) {
            binders.offer(binder);
            int priority = PriorityUtils.getPriority(binder.getClass());
            log.debug("加载参数绑定器: {} (priority={})", binder.getClass().getName(), priority);
        }

        log.info("DefaultParameterBinderRegistry 共注册 {} 个参数绑定器", binders.size());
    }

    @Override
    public Object resolve(ParameterMetadata metadata,
                         HttpServletRequest request,
                         HttpServletResponse response,
                         Map<String, String> pathVariables) throws Exception {

        Parameter param = metadata.parameter();

        ParameterBinder cached = cache.get(param);
        if (cached != null) {
            log.debug("从缓存命中参数绑定器: {}", metadata.name());
            return cached.bind(metadata, request, response, pathVariables);
        }

        for (ParameterBinder binder : binders) {
            if (binder.supports(metadata)) {
                cache.put(param, binder);
                log.debug("使用参数绑定器处理: {} -> {}",
                         metadata.name(), binder.getClass().getName());
                return binder.bind(metadata, request, response, pathVariables);
            }
        }

        throw new IllegalStateException(
            "No suitable ParameterBinder found for parameter: " + metadata.name());
    }
}
