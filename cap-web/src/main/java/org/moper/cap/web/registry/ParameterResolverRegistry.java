package org.moper.cap.web.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.moper.cap.web.model.ParameterMetadata;
import org.moper.cap.web.resolver.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 参数解析器注册表。
 *
 * <p>管理所有 {@link ParameterResolver} 实现，并按顺序尝试解析方法参数。
 */
public class ParameterResolverRegistry {

    private final List<ParameterResolver> resolvers = new ArrayList<>();

    /**
     * 使用默认解析器集初始化注册表。
     *
     * @param objectMapper Jackson ObjectMapper，用于请求体反序列化
     */
    public ParameterResolverRegistry(ObjectMapper objectMapper) {
        resolvers.add(new ServletRequestResolver());
        resolvers.add(new ServletResponseResolver());
        resolvers.add(new PathVariableResolver());
        resolvers.add(new RequestParamResolver());
        resolvers.add(new RequestBodyResolver(objectMapper));
        resolvers.add(new RequestHeaderResolver());
        resolvers.add(new CookieValueResolver());
    }

    /**
     * 注册自定义参数解析器（添加至末尾）。
     *
     * @param resolver 解析器实现
     */
    public void addResolver(ParameterResolver resolver) {
        resolvers.add(resolver);
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
}
