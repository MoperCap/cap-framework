package org.moper.cap.web.handler.parameter.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.moper.cap.common.converter.TypeResolver;
import org.moper.cap.common.priority.Priority;
import org.moper.cap.web.annotation.request.RequestHeader;
import org.moper.cap.web.handler.parameter.ParameterMetadata;
import org.moper.cap.web.handler.parameter.ParameterResolver;

import java.util.Map;

/**
 * 请求头解析器。
 *
 * <p>将 HTTP 请求头的值绑定到标注了 {@link RequestHeader} 的方法参数。
 */
@Priority(60)
public class RequestHeaderResolver implements ParameterResolver {

    private TypeResolver typeResolver;

    public RequestHeaderResolver() {
    }

    public RequestHeaderResolver(TypeResolver typeResolver) {
        this.typeResolver = typeResolver;
    }

    public void setTypeResolver(TypeResolver typeResolver) {
        this.typeResolver = typeResolver;
    }

    @Override
    public boolean supports(ParameterMetadata metadata) {
        return metadata.parameter().isAnnotationPresent(RequestHeader.class);
    }

    @Override
    public Object resolve(ParameterMetadata metadata,
                          HttpServletRequest request,
                          HttpServletResponse response,
                          Map<String, String> pathVariables) {
        RequestHeader annotation = metadata.parameter().getAnnotation(RequestHeader.class);
        String name = resolveName(annotation, metadata);
        String value = request.getHeader(name);
        if (value == null) {
            if (!annotation.defaultValue().isEmpty()) {
                value = annotation.defaultValue();
            } else if (annotation.required()) {
                throw new IllegalStateException("Required request header '" + name + "' not present");
            } else {
                return null;
            }
        }
        return typeResolver.resolve(value, metadata.type());
    }

    private String resolveName(RequestHeader annotation, ParameterMetadata metadata) {
        String name = annotation.value().isBlank() ? annotation.name() : annotation.value();
        return name.isBlank() ? metadata.name() : name;
    }
}
