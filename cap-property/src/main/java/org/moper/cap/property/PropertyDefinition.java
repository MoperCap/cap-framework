package org.moper.cap.property;

import java.time.Instant;

/**
 * 属性字段定义
 *
 * @param key 属性键，唯一标识一个属性，不允许重复，不允许为空或者空白
 * @param value 属性值，可以是任意类型，允许为空
 * @param publisher 属性发布者，唯一标识一个属性发布者，不允许为空或者空白
 * @param lastModified 最后修改时间，表示属性的最后修改时间，不允许为空
 */
public record PropertyDefinition(
        String key,
        Object value,
        String publisher,
        Instant lastModified
) {
    public PropertyDefinition{

        if(key == null || key.isBlank()){
            throw new IllegalArgumentException("PropertyDefinition Key cannot be null or blank");
        }

        if(publisher == null || publisher.isBlank()){
            throw new IllegalArgumentException("PropertyDefinition Publisher cannot be null or blank");
        }

        if(lastModified == null){
            throw new IllegalArgumentException("PropertyDefinition LastModified cannot be null");
        }
    }

    /**
     * 属性字段定义工厂方法，简化属性字段定义的创建过程，自动设置最后修改时间为当前时间。
     *
     * @param key 属性键，唯一标识一个属性，不允许重复，不允许为空或者空白
     * @param value 属性值，可以是任意类型，允许为空
     * @param publisher 属性发布者，唯一标识一个属性发布者，不允许为空或者空白
     * @return 一个新的属性字段定义实例，包含指定的属性键、属性值、属性发布者和当前时间作为最后修改时间
     */
    public static PropertyDefinition of(String key, Object value, String publisher) {
        return new PropertyDefinition(key, value, publisher, Instant.now());
    }

    /**
     * 创建一个新的属性字段定义实例，包含相同的属性键、属性发布者和当前时间作为最后修改时间，但使用新的属性值。
     *
     * @param newValue 新的属性值，可以是任意类型，允许为空
     * @return 一个新的属性字段定义实例，包含相同的属性键、属性发布者和当前时间作为最后修改时间，但使用新的属性值
     */
    public PropertyDefinition withValue(Object newValue) {
        return new PropertyDefinition(key, newValue, publisher, Instant.now());
    }

}
