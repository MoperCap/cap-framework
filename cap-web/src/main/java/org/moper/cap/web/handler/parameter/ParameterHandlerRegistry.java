package org.moper.cap.web.handler.parameter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.converter.TypeResolver;
import org.moper.cap.common.priority.PriorityUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 参数处理器注册表（SPI 模式）。
 *
 * <p>使用 {@link ServiceLoader} 自动发现所有 {@link ParameterHandler} 实现，
 * 通过 {@link PriorityQueue} 按优先级排序，并使用 {@link ConcurrentHashMap} 缓存已匹配的处理器。
 */
@Slf4j
public class ParameterHandlerRegistry {

    private final Queue<ParameterHandler> handlers;
    private final Map<Parameter, ParameterHandler> cache;

    /**
     * 使用 ServiceLoader 加载并按优先级排序的方式初始化注册表。
     *
     * @param objectMapper Jackson ObjectMapper，用于请求体反序列化
     * @param typeResolver 类型解析器，用于参数类型转换
     */
    public ParameterHandlerRegistry(ObjectMapper objectMapper, TypeResolver typeResolver) {
        this.handlers = new PriorityQueue<>(
                Comparator.comparingInt(h -> PriorityUtils.getPriority(h.getClass()))
        );
        this.cache = new ConcurrentHashMap<>();

        ServiceLoader<ParameterHandler> loader = ServiceLoader.load(ParameterHandler.class);
        for (ParameterHandler handler : loader) {
            injectDependencies(handler, objectMapper, typeResolver);
            handlers.offer(handler);

            int priority = PriorityUtils.getPriority(handler.getClass());
            log.debug("注册参数处理器: {} (priority={})", handler.getClass().getName(), priority);
        }

        log.info("ParameterHandlerRegistry 共注册 {} 个参数处理器", handlers.size());
    }

    /**
     * 解析单个方法参数。
     *
     * @param metadata      参数元数据
     * @param request       HTTP 请求
     * @param response      HTTP 响应
     * @param pathVariables 已提取的路径变量
     * @return 解析出的参数值
     * @throws Exception 若解析失败或参数不被支持
     */
    public Object resolve(ParameterMetadata metadata,
                          HttpServletRequest request,
                          HttpServletResponse response,
                          Map<String, String> pathVariables) throws Exception {
        Parameter param = metadata.parameter();

        ParameterHandler cached = cache.get(param);
        if (cached != null) {
            return cached.resolve(metadata, request, response, pathVariables);
        }

        for (ParameterHandler handler : handlers) {
            if (handler.supports(metadata)) {
                cache.put(param, handler);
                return handler.resolve(metadata, request, response, pathVariables);
            }
        }

        throw new IllegalStateException(
                "No suitable ParameterHandler found for parameter: " + metadata.name());
    }

    private void injectDependencies(ParameterHandler handler,
                                    ObjectMapper objectMapper,
                                    TypeResolver typeResolver) {
        try {
            Method method = handler.getClass().getMethod("setObjectMapper", ObjectMapper.class);
            method.invoke(handler, objectMapper);
        } catch (Exception ignored) {
        }

        try {
            Method method = handler.getClass().getMethod("setTypeResolver", TypeResolver.class);
            method.invoke(handler, typeResolver);
        } catch (Exception ignored) {
        }
    }
}
