package org.moper.cap.property.resolver;

/**
 * 属性类型转换器  </br>
 * 负责从源类型转换为目标类型，支持SPI方式自动发现。
 *
 * @param <S> 源类型
 * @param <T> 目标类型
 */
public interface PropertyConverter<S, T> extends Comparable<PropertyConverter<S, T>> {

    /**
     * 源类型
     */
    Class<S> getSourceType();

    /**
     * 目标类型
     */
    Class<T> getTargetType();

    /**
     * 同类型转换器之间的优先级别 </br>
     * 优先级别数值小的优先使用，默认值为100
     * 注意：这里的同类别指的是源类型和目标类型都相同的转换器
     *
     * @return 优先级别数值，数值小的优先使用
     */
    default int getOrder(){
        return 100;
    }

    /**
     * 转换逻辑。
     */
    T convert(S value) throws Exception;

    @Override
    default int compareTo(PropertyConverter<S, T> o){
        return Integer.compare(getOrder(), o.getOrder());
    }
}
