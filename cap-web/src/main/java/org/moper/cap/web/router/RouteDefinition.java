package org.moper.cap.web.router;

import org.moper.cap.web.http.HttpMethod;
import org.moper.cap.web.binder.ParameterMetadata;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public record RouteDefinition(
        String path,
        HttpMethod httpMethod,
        Object controller,
        Method controllerMethod,
        List<ParameterMetadata> parameters
) {

    private static final ConcurrentHashMap<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();

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
    }

    public boolean matches(String requestPath, HttpMethod method) {
        if (httpMethod != method) {
            return false;
        }
        if (path.equals(requestPath)) {
            return true;
        }
        return getOrBuildPattern(path).matcher(requestPath).matches();
    }

    public Map<String, String> extractPathVariables(String requestPath) {
        Map<String, String> variables = new HashMap<>();
        String[] templateParts = path.split("/");
        String[] requestParts = requestPath.split("/");
        if (templateParts.length != requestParts.length) {
            return variables;
        }
        for (int i = 0; i < templateParts.length; i++) {
            String part = templateParts[i];
            if (part.startsWith("{") && part.endsWith("}")) {
                String varName = part.substring(1, part.length() - 1);
                variables.put(varName, requestParts[i]);
            }
        }
        return variables;
    }

    private static Pattern getOrBuildPattern(String pathTemplate) {
        return PATTERN_CACHE.computeIfAbsent(pathTemplate, RouteDefinition::buildPathPattern);
    }

    private static Pattern buildPathPattern(String pathTemplate) {
        String[] parts = pathTemplate.split("/", -1);
        StringBuilder regex = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                regex.append("/");
            }
            String part = parts[i];
            if (part.startsWith("{") && part.endsWith("}")) {
                regex.append("[^/]+");
            } else {
                regex.append(Pattern.quote(part));
            }
        }
        return Pattern.compile(regex.toString());
    }
}
