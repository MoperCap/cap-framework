package org.moper.cap.bean.factory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jetbrains.annotations.Nullable;
import org.moper.cap.bean.exception.BeanException;

/**
 * Bean工厂基础接口
 * 提供基本的Bean访问能力
 */
public interface BeanFactory {
    /**
     * 根据Bean名称获取Bean实例
     *
     * @param beanName Bean名称
     * @return Bean实例
     * @throws BeanException 若Bean不存在或获取失败
     */
    @NotNull Object getBean(@NotBlank String beanName) throws BeanException;

    /**
     * 根据Bean名称和类型获取Bean实例
     *
     * @param beanName Bean名称
     * @param requiredType 要求的类型
     * @param <T> 类型参数
     * @return Bean实例
     * @throws BeanException 若Bean不存在、类型不匹配或获取失败
     */
    <T> @NotNull T getBean(@NotBlank String beanName, @NotNull Class<T> requiredType) throws BeanException;

    /**
     * 根据类型获取Bean实例
     * 如果存在多个同类型Bean，则返回标记为primary的Bean
     *
     * @param requiredType 要求的类型
     * @param <T> 类型参数
     * @return Bean实例
     * @throws BeanException 若Bean不存在、存在多个且无primary标记或获取失败
     */
    <T> @NotNull T getBean(@NotNull Class<T> requiredType) throws BeanException;

    /**
     * 检查是否包含指定名称的Bean定义
     *
     * @param beanName Bean名称
     * @return 是否包含
     */
    boolean containsBean(@NotBlank String beanName);

    /**
     * 检查指定名称的Bean是否为单例
     *
     * @param beanName Bean名称
     * @return 是否为单例
     * @throws BeanException 若Bean不存在
     */
    boolean isSingleton(@NotBlank String beanName) throws BeanException;

    /**
     * 检查指定名称的Bean是否为原型
     *
     * @param beanName Bean名称
     * @return 是否为原型
     * @throws BeanException 若Bean不存在
     */
    boolean isPrototype(@NotBlank String beanName) throws BeanException;

    /**
     * 检查指定名称的Bean是否与给定类型匹配
     *
     * @param beanName Bean名称
     * @param targetType 目标类型
     * @return 是否匹配
     * @throws BeanException 若Bean不存在
     */
    boolean isTypeMatch(@NotBlank String beanName, @NotNull Class<?> targetType) throws BeanException;

    /**
     * 获取指定名称的Bean的类型
     *
     * @param beanName Bean名称
     * @return Bean类型，若Bean不存在则返回null
     */
    @Nullable Class<?> getType(@NotBlank String beanName);

    /**
     * 获取指定名称的Bean的所有别名
     *
     * @param beanName Bean名称
     * @return 别名数组
     */
    @NotNull String[] getAliases(@NotBlank String beanName);
}
