package org.moper.cap.web.binder.impl;

import org.moper.cap.web.binder.ParameterBinder;
import org.moper.cap.web.binder.ParameterMetadata;
import org.moper.cap.web.annotation.request.RequestParam;
import org.moper.cap.common.priority.Priority;
import org.moper.cap.common.converter.TypeResolverFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

@Priority(100)
public class RequestParamBinder implements ParameterBinder {

    @Override
    public boolean supports(ParameterMetadata metadata) {
        return metadata.parameter().isAnnotationPresent(RequestParam.class);
    }

    @Override
    public Object bind(ParameterMetadata metadata,
                      HttpServletRequest request,
                      HttpServletResponse response,
                      Map<String, String> pathVariables) throws Exception {

        RequestParam annotation = metadata.parameter().getAnnotation(RequestParam.class);
        String name = annotation.value().isBlank() ?
                      (annotation.name().isBlank() ? metadata.name() : annotation.name()) :
                      annotation.value();

        String value = request.getParameter(name);

        if (value == null) {
            if (!annotation.defaultValue().isEmpty()) {
                value = annotation.defaultValue();
            } else if (annotation.required()) {
                throw new IllegalArgumentException("Required request param not found: " + name);
            } else {
                return null;
            }
        }

        return TypeResolverFactory.getTypeResolver().resolve(value, metadata.type());
    }
}
