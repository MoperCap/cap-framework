package org.moper.cap.web.util;

import org.moper.cap.web.annotation.request.PathVariable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public final class PathVariableResolver {

    /**
     * 从方法中解析所有路径变量名
     */
    public static List<String> resolvePathVariableNames(Method method) {
        List<String> names = new ArrayList<>();

        if (method == null) {
            return names;
        }

        Parameter[] parameters = method.getParameters();
        for (Parameter param : parameters) {
            String varName = resolve(param);
            if (varName != null) {
                names.add(varName);
            }
        }

        return names;
    }

    /**
     * 从单个参数中解析路径变量名
     * 如果参数没有 @PathVariable 注解，返回 null
     */
    public static String resolve(Parameter parameter) {
        if (parameter == null) {
            return null;
        }

        if (!parameter.isAnnotationPresent(PathVariable.class)) {
            return null;
        }

        PathVariable annotation = parameter.getAnnotation(PathVariable.class);
        String annotationValue = annotation.value();

        // 如果注解有明确的值，使用注解值；否则使用参数名
        return annotationValue.isBlank() ? parameter.getName() : annotationValue;
    }

    /**
     * 检查参数是否被 @PathVariable 注解
     */
    public static boolean isPathVariable(Parameter parameter) {
        return parameter != null && parameter.isAnnotationPresent(PathVariable.class);
    }
}
