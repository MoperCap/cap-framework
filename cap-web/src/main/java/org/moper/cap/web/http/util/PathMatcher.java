package org.moper.cap.web.http.util;

import java.util.regex.Pattern;

/**
 * HTTP 请求路径匹配工具类。
 *
 * <p>支持含路径变量（如 {@code {id}}）的模板与实际请求路径的匹配。
 */
public final class PathMatcher {

    private static final Pattern PATH_VARIABLE_PATTERN = Pattern.compile("\\{([^/]+?)}");

    private PathMatcher() {
    }

    /**
     * 判断请求路径是否匹配路径模板。
     *
     * <p>路径变量（如 {@code {id}}）可匹配任意非斜线字符序列。
     *
     * @param pattern     路径模板，如 {@code "/api/users/{id}"}
     * @param requestPath 实际请求路径，如 {@code "/api/users/42"}
     * @return 如果匹配则返回 {@code true}，否则返回 {@code false}
     */
    public static boolean matches(String pattern, String requestPath) {
        if (pattern == null || requestPath == null) {
            return false;
        }
        return toRegex(pattern).matcher(requestPath).matches();
    }

    /**
     * 将路径模板转换为正则表达式。
     *
     * @param pathTemplate 路径模板
     * @return 对应的正则表达式 Pattern
     */
    public static Pattern toRegex(String pathTemplate) {
        StringBuilder regex = new StringBuilder();
        java.util.regex.Matcher varMatcher = PATH_VARIABLE_PATTERN.matcher(pathTemplate);
        int lastEnd = 0;
        while (varMatcher.find()) {
            regex.append(Pattern.quote(pathTemplate.substring(lastEnd, varMatcher.start())));
            regex.append("([^/]+)");
            lastEnd = varMatcher.end();
        }
        regex.append(Pattern.quote(pathTemplate.substring(lastEnd)));
        return Pattern.compile(regex.toString());
    }
}
