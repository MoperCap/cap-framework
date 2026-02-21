package org.moper.cap.bean.processor;

import jakarta.validation.constraints.NotNull;
import org.moper.cap.bean.exception.BeanException;
import org.moper.cap.bean.factory.ConfigurableBeanFactory;

/**
 * Bean工厂后置处理器接口
 * 在Bean实例化之前修改Bean定义
 */
public interface BeanFactoryPostProcessor {
    /**
     * 在所有Bean定义加载完成但未实例化之前调用
     *
     * @param beanFactory Bean工厂
     * @throws BeanException 若处理失败
     */
    void postProcessBeanFactory(@NotNull ConfigurableBeanFactory beanFactory) throws BeanException;
}
