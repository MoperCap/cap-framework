package org.moper.cap.web.binder;

import java.lang.reflect.Parameter;

/**
 * 参数元数据
 *
 * <p>注意：参数名的获取依赖于编译时的 {@code -parameters} 选项。
 * 如果参数名为 {@code arg0}、{@code arg1} 等，说明编译时没有启用 {@code -parameters}。
 */
public record ParameterMetadata(
        Parameter parameter,
        String name,
        Class<?> type
) {
    public ParameterMetadata {
        if (parameter == null) {
            throw new IllegalArgumentException("parameter cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name cannot be blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
    }

    /**
     * 获取参数名称，并附加调试信息。
     *
     * <p>若参数名匹配编译器生成的默认格式（如 {@code arg0}），
     * 则附加警告提示，说明需要启用 {@code -parameters} 编译选项。
     */
    public String getNameWithDebug() {
        boolean isCompiledName = name.matches("arg\\d+");
        return name + (isCompiledName ? " (WARNING: compiled parameter name detected, enable -parameters compiler option)" : "");
    }
}
