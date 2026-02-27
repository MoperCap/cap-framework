package org.moper.cap.property.officer;

import org.moper.cap.property.PropertyDefinition;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 属性视图接口 </br>
 *
 * 用于提供属性的获取能力
 */
public interface PropertyView {

    /**
     * 获取指定属性键对应的的原始值
     *
     * @param key 属性键，不能为null或blank
     * @return 若存在对应的属性，则返回原始值；否则返回null
     */
    Object getRawPropertyValue(String key);

    /**
     * 获取指定属性键对应的指定类型属性值
     *
     * @param key 属性键，不能为null或blank
     * @param type 指定类型，不能为null
     * @return 若存在对应的属性且类型转换成功则返回；否则返回null
     * @param <T> 指定类型
     */
    <T>  T getPropertyValue(String key,  Class<T> type);

    /**
     * 获取指定属性键对应的指定类型属性值
     *
     * @param key 属性键，不能为null或blank
     * @param type 指定类型，不能为null
     * @param defaultValue 默认值，不能为null
     * @return 若存在对应的属性且类型转换成功则返回；否则返回默认值
     * @param <T> 指定类型
     */
    <T>  T getPropertyValueOrDefault(String key,  Class<T> type,  T defaultValue);

    /**
     * 获取指定属性键对应的指定类型Optional属性
     *
     * @param key 属性键，不能为null或blank
     * @param type 指定类型，不能为null
     * @return 若存在对应的属性且类型转换成功则返回；否则返回Optional.empty()
     * @param <T> 指定类型
     */
    <T> Optional<T> getPropertyValueOptional(String key, Class<T> type);

    /**
     * 检查是否存在指定属性键对应的属性
     *
     * @param key 属性键，不能为null或blank
     * @return 若存在则返回true；否则返回false
     */
    boolean containsProperty(String key);

    /**
     * 获取所有属性的属性键的集合
     *
     * @return 属性键集合。若不包含任何属性，则返回空集合
     */
    Set<String> getAllPropertyKey();
}
