package org.moper.cap.web.binder.impl;

import org.moper.cap.web.binder.ParameterBinder;
import org.moper.cap.web.binder.ParameterMetadata;
import org.moper.cap.web.annotation.request.PathVariable;
import org.moper.cap.common.priority.Priority;
import org.moper.cap.common.converter.TypeResolverFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@Priority(100)
public class PathVariableBinder implements ParameterBinder {

    @Override
    public boolean supports(ParameterMetadata metadata) {
        return metadata.parameter().isAnnotationPresent(PathVariable.class);
    }

    @Override
    public Object bind(ParameterMetadata metadata,
                      HttpServletRequest request,
                      HttpServletResponse response,
                      Map<String, String> pathVariables) throws Exception {

        PathVariable annotation = metadata.parameter().getAnnotation(PathVariable.class);
        String name = annotation.value().isBlank() ?
                      (annotation.name().isBlank() ? metadata.name() : annotation.name()) :
                      annotation.value();

        log.debug("PathVariable binding: annotation.value='{}', annotation.name='{}', parameter name='{}', resolved name='{}'",
                annotation.value(), annotation.name(), metadata.getNameWithDebug(), name);
        log.debug("Available path variables: {}", pathVariables);

        String value = pathVariables.get(name);

        if (value == null && annotation.required()) {
            log.error("Path variable not found: {} (available variables: {})", name, pathVariables.keySet());
            throw new IllegalArgumentException("Required path variable not found: " + name);
        }

        if (value == null) {
            return null;
        }

        return TypeResolverFactory.getTypeResolver().resolve(value, metadata.type());
    }
}
