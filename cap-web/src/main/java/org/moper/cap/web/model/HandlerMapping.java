package org.moper.cap.web.model;

import org.moper.cap.web.http.HttpMethod;
import org.moper.cap.web.parameter.ParameterMetadata;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 路由映射信息的完整描述（不可变）。
 *
 * <p>保存控制器方法与 HTTP 路径、方法及媒体类型之间的映射关系，
 * 并提供请求匹配和路径变量提取功能。
 *
 * <p><b>使用示例：</b>
 * <pre>{@code
 * HandlerMapping mapping = new HandlerMapping(
 *     "/api/users/{id}",
 *     HttpMethod.GET,
 *     "application/json",
 *     null,
 *     controllerInstance,
 *     getUserMethod,
 *     parameterList,
 *     List.of("id")
 * );
 *
 * boolean matched = mapping.matches("/api/users/42", HttpMethod.GET);
 * Map<String, String> vars = mapping.extractPathVariables("/api/users/42");
 * }</pre>
 *
 * @param path              路由路径模板（如 {@code "/api/users/{id}"}）
 * @param httpMethod        限定的 HTTP 方法
 * @param produces          响应的媒体类型（如 {@code "application/json"}），可为 null
 * @param consumes          请求接受的媒体类型（如 {@code "application/json"}），可为 null
 * @param handler           控制器实例，不能为 null
 * @param handlerMethod     要调用的方法，不能为 null
 * @param parameters        方法参数元数据列表，不能为 null
 * @param pathVariableNames 从路径模板中提取的路径变量名称列表（如 {@code ["id"]}），不能为 null
 */
public record HandlerMapping(
        String path,
        HttpMethod httpMethod,
        String produces,
        String consumes,
        Object handler,
        Method handlerMethod,
        List<ParameterMetadata> parameters,
        List<String> pathVariableNames
) {

    private static final Pattern PATH_VARIABLE_PATTERN = Pattern.compile("\\{([^/]+?)}");

    public HandlerMapping {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("path must not be blank");
        }
        if (httpMethod == null) {
            throw new IllegalArgumentException("httpMethod must not be null");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler must not be null");
        }
        if (handlerMethod == null) {
            throw new IllegalArgumentException("handlerMethod must not be null");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("parameters must not be null");
        }
        if (pathVariableNames == null) {
            throw new IllegalArgumentException("pathVariableNames must not be null");
        }
        parameters = List.copyOf(parameters);
        pathVariableNames = List.copyOf(pathVariableNames);
    }

    /**
     * 判断给定的请求路径和 HTTP 方法是否与本映射匹配。
     *
     * <p>路径匹配时，路径变量（如 {@code {id}}）可匹配任意非斜线字符串。
     *
     * @param requestPath   请求路径，不能为 null
     * @param requestMethod 请求的 HTTP 方法，不能为 null
     * @return 如果路径和方法均匹配则返回 {@code true}，否则返回 {@code false}
     */
    public boolean matches(String requestPath, HttpMethod requestMethod) {
        if (requestPath == null || requestMethod == null) {
            return false;
        }
        if (this.httpMethod != requestMethod) {
            return false;
        }
        return toRegex(this.path).matcher(requestPath).matches();
    }

    /**
     * 从给定的请求路径中提取路径变量。
     *
     * <p>例如，模板 {@code "/api/users/{id}"} 对路径 {@code "/api/users/42"} 的提取结果为
     * {@code {"id" -> "42"}}。
     *
     * @param requestPath 请求路径，不能为 null
     * @return 路径变量名称到值的映射；若路径不匹配模板，则返回空 Map
     */
    public Map<String, String> extractPathVariables(String requestPath) {
        Map<String, String> result = new HashMap<>();
        if (requestPath == null) {
            return result;
        }
        Pattern regex = toRegex(this.path);
        Matcher matcher = regex.matcher(requestPath);
        if (matcher.matches()) {
            for (int i = 0; i < pathVariableNames.size(); i++) {
                result.put(pathVariableNames.get(i), matcher.group(i + 1));
            }
        }
        return result;
    }

    private static Pattern toRegex(String pathTemplate) {
        StringBuilder regex = new StringBuilder();
        Matcher varMatcher = PATH_VARIABLE_PATTERN.matcher(pathTemplate);
        int lastEnd = 0;
        while (varMatcher.find()) {
            regex.append(Pattern.quote(pathTemplate.substring(lastEnd, varMatcher.start())));
            regex.append("([^/]+)");
            lastEnd = varMatcher.end();
        }
        regex.append(Pattern.quote(pathTemplate.substring(lastEnd)));
        return Pattern.compile(regex.toString());
    }

    private static List<String> extractVariableNames(String pathTemplate) {
        List<String> names = new ArrayList<>();
        Matcher matcher = PATH_VARIABLE_PATTERN.matcher(pathTemplate);
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names;
    }
}
