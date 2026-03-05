package org.moper.cap.web.binder.impl;

import org.moper.cap.web.binder.ParameterBinder;
import org.moper.cap.web.binder.ParameterMetadata;
import org.moper.cap.web.annotation.request.RequestHeader;
import org.moper.cap.common.priority.Priority;
import org.moper.cap.common.converter.TypeResolver;
import org.moper.cap.common.converter.impl.DefaultTypeResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

@Priority(100)
public class RequestHeaderBinder implements ParameterBinder {

    private final TypeResolver typeResolver;

    public RequestHeaderBinder() {
        this.typeResolver = new DefaultTypeResolver();
    }

    @Override
    public boolean supports(ParameterMetadata metadata) {
        return metadata.parameter().isAnnotationPresent(RequestHeader.class);
    }

    @Override
    public Object bind(ParameterMetadata metadata,
                      HttpServletRequest request,
                      HttpServletResponse response,
                      Map<String, String> pathVariables) throws Exception {

        RequestHeader annotation = metadata.parameter().getAnnotation(RequestHeader.class);
        String name = annotation.value().isBlank() ?
                      (annotation.name().isBlank() ? metadata.name() : annotation.name()) :
                      annotation.value();

        String value = request.getHeader(name);

        if (value == null) {
            if (!annotation.defaultValue().isEmpty()) {
                value = annotation.defaultValue();
            } else if (annotation.required()) {
                throw new IllegalArgumentException("Required request header not found: " + name);
            } else {
                return null;
            }
        }

        return typeResolver.resolve(value, metadata.type());
    }
}
