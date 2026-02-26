package org.moper.cap.property.resolver;

/**
 * 属性类型转换器抽象类，提供了源类型和目标类型的基本实现，子类只需实现转换逻辑。
 *
 * @param <S> 源类型
 * @param <T> 目标类型
 */
public abstract class AbstractPropertyConverter<S, T> implements PropertyConverter<S, T> {
    private final Class<S> sourceType;
    private final Class<T> targetType;

    public AbstractPropertyConverter(Class<S> sourceType, Class<T> targetType) {
        this.sourceType = sourceType;
        this.targetType = targetType;
    }

    /**
     * 源类型
     */
    @Override
    public Class<S> getSourceType() {
        return sourceType;
    }

    /**
     * 目标类型
     */
    @Override
    public Class<T> getTargetType() {
        return targetType;
    }
}
