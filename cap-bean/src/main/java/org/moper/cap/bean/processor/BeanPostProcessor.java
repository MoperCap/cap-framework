package org.moper.cap.bean.processor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jetbrains.annotations.Nullable;
import org.moper.cap.bean.exception.BeanException;

/**
 * Bean后置处理器接口
 * 在Bean初始化前后提供自定义处理逻辑
 */
public interface BeanPostProcessor {
    /**
     * 在Bean初始化之前调用
     *
     * @param bean Bean实例
     * @param beanName Bean名称
     * @return 处理后的Bean实例（可以返回原实例或包装/代理实例）
     * @throws BeanException 若处理失败
     */
    default @Nullable Object postProcessBeforeInitialization(@NotNull Object bean, @NotBlank String beanName) throws BeanException {
        return bean;
    }

    /**
     * 在Bean初始化之后调用
     *
     * @param bean Bean实例
     * @param beanName Bean名称
     * @return 处理后的Bean实例（可以返回原实例或包装/代理实例）
     * @throws BeanException 若处理失败
     */
    default @Nullable Object postProcessAfterInitialization(@NotNull Object bean, @NotBlank String beanName) throws BeanException {
        return bean;
    }
}
