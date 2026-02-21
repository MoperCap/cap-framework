package org.moper.cap.bean.processor;

import jakarta.validation.constraints.NotNull;
import org.moper.cap.bean.registry.BeanDefinitionRegistry;
import org.moper.cap.core.exception.CapFrameworkException;

/**
 * Bean定义注册后置处理器接口
 * 在所有Bean定义加载完成后，允许修改或添加Bean定义
 */
public interface BeanDefinitionRegistryPostProcessor {

    /**
     * 在所有Bean定义加载完成后调用
     *
     * @param registry Bean定义注册表
     * @throws CapFrameworkException 若处理失败
     */
    void postProcessBeanDefinitionRegistry(@NotNull BeanDefinitionRegistry registry) throws CapFrameworkException;
}
