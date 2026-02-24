package org.moper.cap.property.resolver;

/**
 * 属性类型转换器  </br>
 * 负责从源类型转换为目标类型，支持SPI方式自动发现。
 *
 * @param <S> 源类型
 * @param <T> 目标类型
 */
public interface PropertyConverter<S, T> {

    /**
     * 源类型
     */
    Class<S> getSourceType();

    /**
     * 目标类型
     */
    Class<T> getTargetType();

    /**
     * 转换逻辑。
     */
    T convert(S value) throws Exception;
}
