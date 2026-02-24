package org.moper.cap.property.resolver;

/**
 * 属性类型解析转换器
 */
public interface PropertyResolver {

    /**
     * 类型安全地将对象value转换为目标类型targetType。
     * 找不到合适转换器或转换失败时抛出异常。
     */
    <T> T resolve(Object value, Class<T> targetType);

    /**
     * 判定是否存在从sourceType到targetType的转换器。
     */
    boolean hasConverter(Class<?> sourceType, Class<?> targetType);
}