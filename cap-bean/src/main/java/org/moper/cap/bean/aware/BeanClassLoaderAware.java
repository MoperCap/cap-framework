package org.moper.cap.bean.aware;

import jakarta.validation.constraints.NotNull;

/**
 * Bean类加载器感知接口
 * 实现此接口的Bean可以获取到类加载器的引用
 */
public interface BeanClassLoaderAware {

    /**
     * 设置类加载器
     *
     * @param classLoader 类加载器
     */
    void setBeanClassLoader(@NotNull ClassLoader classLoader);
}
