package org.moper.cap.environment;

import org.jetbrains.annotations.Nullable;

/**
 * 应用环境接口，提供属性查询和属性源管理能力
 */
public interface Environment {

    /**
     * 获取指定键的属性值
     *
     * @param key 属性键
     * @return 属性值，若不存在则返回 null
     */
    @Nullable String getProperty(String key);

    /**
     * 获取指定键的属性值，若不存在则返回默认值
     *
     * @param key          属性键
     * @param defaultValue 默认值
     * @return 属性值或默认值
     */
    String getProperty(String key, String defaultValue);

    /**
     * 获取指定键的属性值并转换为目标类型
     *
     * @param key        属性键
     * @param targetType 目标类型
     * @param <T>        类型参数
     * @return 属性值，若不存在则返回 null
     */
    <T> @Nullable T getProperty(String key, Class<T> targetType);

    /**
     * 判断是否包含指定键的属性
     *
     * @param key 属性键
     * @return 若包含则返回 true
     */
    boolean containsProperty(String key);

    /**
     * 添加属性源
     *
     * @param propertySource 要添加的属性源
     */
    void addPropertySource(PropertySource propertySource);
}
