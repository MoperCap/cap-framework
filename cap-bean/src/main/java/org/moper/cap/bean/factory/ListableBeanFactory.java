package org.moper.cap.bean.factory;

import jakarta.validation.constraints.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * 可列举的Bean工厂接口
 * 提供批量查询Bean的能力
 */
public interface ListableBeanFactory extends BeanFactory {
    /**
     * 获取所有Bean定义的数量
     *
     * @return Bean定义数量
     */
    int getBeanDefinitionCount();

    /**
     * 获取所有Bean定义的名称
     *
     * @return Bean定义名称数组
     */
    @NotNull String[] getBeanDefinitionNames();

    /**
     * 根据类型获取所有Bean名称
     *
     * @param type 类型
     * @return Bean名称数组
     */
    @NotNull String[] getBeanNamesForType(@NotNull Class<?> type);

    /**
     * 根据类型获取所有Bean实例
     *
     * @param type 类型
     * @param <T> 类型参数
     * @return Bean名称到实例的映射
     * @throws BeanException 若获取失败
     */
    <T> @NotNull Map<String, T> getBeansOfType(@NotNull Class<T> type) throws BeanException;

    /**
     * 根据注解类型获取所有标注了该注解的Bean名称
     *
     * @param annotationType 注解类型
     * @return Bean名称数组
     */
    @NotNull String[] getBeanNamesForAnnotation(@NotNull Class<? extends Annotation> annotationType);

    /**
     * 根据注解类型获取所有标注了该注解的Bean实例
     *
     * @param annotationType 注解类型
     * @return Bean名称到实例的映射
     * @throws BeanException 若获取失败
     */
    @NotNull Map<String, Object> getBeansWithAnnotation(@NotNull Class<? extends Annotation> annotationType) throws BeanException;

    /**
     * 在指定Bean上查找注解
     *
     * @param beanName Bean名称
     * @param annotationType 注解类型
     * @param <A> 注解类型参数
     * @return 注解实例，若不存在则返回null
     * @throws BeanException 若Bean不存在
     */
    <A extends Annotation> @Nullable A findAnnotationOnBean(@NotNull String beanName, @NotNull Class<A> annotationType) throws BeanException;
}
