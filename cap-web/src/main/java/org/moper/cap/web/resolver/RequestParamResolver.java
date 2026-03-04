package org.moper.cap.web.resolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.moper.cap.common.converter.TypeResolver;
import org.moper.cap.web.annotation.request.RequestParam;
import org.moper.cap.web.model.ParameterMetadata;

import java.util.Map;

/**
 * 请求参数解析器。
 *
 * <p>将 HTTP 查询字符串或表单数据中的参数绑定到标注了 {@link RequestParam} 的方法参数。
 */
public class RequestParamResolver implements ParameterResolver {

    private final TypeResolver typeResolver;

    public RequestParamResolver(TypeResolver typeResolver) {
        this.typeResolver = typeResolver;
    }

    @Override
    public boolean supports(ParameterMetadata metadata) {
        return metadata.parameter().isAnnotationPresent(RequestParam.class);
    }

    @Override
    public Object resolve(ParameterMetadata metadata,
                          HttpServletRequest request,
                          HttpServletResponse response,
                          Map<String, String> pathVariables) {
        RequestParam annotation = metadata.parameter().getAnnotation(RequestParam.class);
        String name = resolveName(annotation, metadata);
        String value = request.getParameter(name);
        if (value == null) {
            if (!annotation.defaultValue().isEmpty()) {
                value = annotation.defaultValue();
            } else if (annotation.required()) {
                throw new IllegalStateException("Required request parameter '" + name + "' not present");
            } else {
                return null;
            }
        }
        return typeResolver.resolve(value, metadata.type());
    }

    private String resolveName(RequestParam annotation, ParameterMetadata metadata) {
        String name = annotation.value().isBlank() ? annotation.name() : annotation.value();
        return name.isBlank() ? metadata.name() : name;
    }
}
