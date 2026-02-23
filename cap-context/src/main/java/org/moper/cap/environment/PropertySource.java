package org.moper.cap.environment;

import org.jetbrains.annotations.Nullable;

/**
 * 属性源接口，提供键值对形式的属性访问能力
 */
public interface PropertySource {

    /**
     * 获取属性源名称
     *
     * @return 属性源名称
     */
    String getName();

    /**
     * 获取指定键的属性值
     *
     * @param key 属性键
     * @return 属性值，若不存在则返回 null
     */
    @Nullable Object getProperty(String key);

    /**
     * 判断是否包含指定键的属性
     *
     * @param key 属性键
     * @return 若包含则返回 true
     */
    boolean containsProperty(String key);

    /**
     * 属性源优先级，值越小优先级越高
     *
     * @return 优先级
     */
    default int getOrder() {
        return 0;
    }
}
