package org.moper.cap.web.handler.parameter;

import java.lang.reflect.Parameter;

/**
 * 方法参数的元数据描述（不可变）。
 *
 * <p>保存控制器方法中单个参数的反射信息，供请求处理时使用。
 *
 * @param index     参数在方法参数列表中的索引（从 0 开始）
 * @param name      参数名称（来自注解或反射）
 * @param type      参数类型
 * @param parameter 原始 {@link Parameter} 反射对象，不能为 null
 */
public record ParameterMetadata(
        int index,
        String name,
        Class<?> type,
        Parameter parameter
) {

    public ParameterMetadata {
        if (parameter == null) {
            throw new IllegalArgumentException("parameter must not be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
    }
}
