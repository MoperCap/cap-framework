package org.moper.cap.web.parameter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.annotation.Priority;
import org.moper.cap.common.converter.TypeResolver;
import org.moper.cap.web.parameter.impl.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * 参数解析器注册表。
 *
 * <p>管理所有 {@link ParameterResolver} 实现，并按优先级（降序）尝试解析方法参数。
 * 使用 {@link ServiceLoader} 加载所有实现，通过 {@link Priority} 注解获取优先级。
 */
@Slf4j
public class ParameterResolverRegistry {

    private final ObjectMapper objectMapper;
    private final TypeResolver typeResolver;
    private volatile List<ParameterResolver> resolvers;

    /**
     * 使用 ServiceLoader 加载并按优先级排序的方式初始化注册表。
     *
     * @param objectMapper Jackson ObjectMapper，用于请求体反序列化
     * @param typeResolver 类型解析器，用于参数类型转换
     */
    public ParameterResolverRegistry(ObjectMapper objectMapper, TypeResolver typeResolver) {
        this.objectMapper = objectMapper;
        this.typeResolver = typeResolver;
        this.resolvers = loadAndSort();
    }

    /**
     * 注册自定义参数解析器（添加至列表并重新排序）。
     *
     * @param resolver 解析器实现
     */
    public void addResolver(ParameterResolver resolver) {
        resolvers.add(resolver);
        resolvers.sort(Comparator.comparingInt(this::getPriority).reversed());
    }

    /**
     * 清除缓存，重新从 ServiceLoader 加载并排序解析器。
     */
    public void clearCache() {
        this.resolvers = loadAndSort();
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
        for (ParameterResolver resolver : resolvers) {
            if (resolver.supports(metadata)) {
                return resolver.resolve(metadata, request, response, pathVariables);
            }
        }
        throw new IllegalStateException(
                "No suitable ParameterResolver found for parameter: " + metadata.name()
                        + " (type: " + metadata.type().getName() + ")");
    }

    private List<ParameterResolver> loadAndSort() {
        List<ParameterResolver> list = new ArrayList<>();
        ServiceLoader.load(ParameterResolver.class).forEach(resolver -> {
            injectDependencies(resolver);
            list.add(resolver);
            log.debug("加载参数解析器: {} (priority={})", resolver.getClass().getName(), getPriority(resolver));
        });
        list.sort(Comparator.comparingInt(this::getPriority).reversed());
        log.info("ParameterResolverRegistry 共加载 {} 个参数解析器", list.size());
        return list;
    }

    private void injectDependencies(ParameterResolver resolver) {
        if (resolver instanceof PathVariableResolver r) {
            r.setTypeResolver(typeResolver);
        } else if (resolver instanceof RequestParamResolver r) {
            r.setTypeResolver(typeResolver);
        } else if (resolver instanceof RequestBodyResolver r) {
            r.setObjectMapper(objectMapper);
        } else if (resolver instanceof RequestHeaderResolver r) {
            r.setTypeResolver(typeResolver);
        } else if (resolver instanceof CookieValueResolver r) {
            r.setTypeResolver(typeResolver);
        }
    }

    private int getPriority(ParameterResolver resolver) {
        Priority priority = resolver.getClass().getAnnotation(Priority.class);
        return priority != null ? priority.value() : 0;
    }
}
