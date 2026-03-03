package org.moper.cap.web.resolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.moper.cap.web.annotation.RequestHeader;
import org.moper.cap.web.model.ParameterMetadata;
import org.moper.cap.web.util.TypeConverter;

import java.util.Map;

/**
 * 请求头解析器。
 *
 * <p>将 HTTP 请求头的值绑定到标注了 {@link RequestHeader} 的方法参数。
 */
public class RequestHeaderResolver implements ParameterResolver {

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
                return TypeConverter.convert(null, metadata.type());
            }
        }
        return TypeConverter.convert(value, metadata.type());
    }

    private String resolveName(RequestHeader annotation, ParameterMetadata metadata) {
        String name = annotation.value().isBlank() ? annotation.name() : annotation.value();
        return name.isBlank() ? metadata.name() : name;
    }
}
