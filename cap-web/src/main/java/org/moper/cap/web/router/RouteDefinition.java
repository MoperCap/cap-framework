package org.moper.cap.web.router;

import org.moper.cap.web.http.HttpMethod;
import org.moper.cap.web.binder.ParameterMetadata;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public record RouteDefinition(
        String path,
        HttpMethod httpMethod,
        Object controller,
        Method controllerMethod,
        List<ParameterMetadata> parameters,
        List<String> pathVariableNames
) {

    public RouteDefinition {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("path cannot be blank");
        }
        if (httpMethod == null) {
            throw new IllegalArgumentException("httpMethod cannot be null");
        }
        if (controller == null) {
            throw new IllegalArgumentException("controller cannot be null");
        }
        if (controllerMethod == null) {
            throw new IllegalArgumentException("controllerMethod cannot be null");
        }

        if (parameters == null) {
            throw new IllegalArgumentException("parameters cannot be null");
        }

        if (pathVariableNames == null) {
            throw new IllegalArgumentException("pathVariableNames cannot be null");
        }
    }

    public boolean matches(String requestPath, HttpMethod method) {
        return httpMethod == method && path.equals(requestPath);
    }
}
