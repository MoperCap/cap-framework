package org.moper.cap.bean.container;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jetbrains.annotations.Nullable;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.exception.BeanException;

import java.lang.annotation.Annotation;

/**
 * Bean检查器接口
 *
 * <p><b>核心语义：提供对容器结构的"查询能力"，但不允许修改</b>
 *
 * <p>此接口可以：
 * <ul>
 *   <li>判断是否存在Bean</li>
 *   <li>查询BeanDefinition</li>
 *   <li>获取某类型的Bean名称</li>
 *   <li>获取标注某注解的Bean名称</li>
 * </ul>
 *
 * <p>此接口不可以：
 * <ul>
 *   <li>注册Bean</li>
 *   <li>删除Bean</li>
 *   <li>创建Bean</li>
 * </ul>
 *
 * <p><b>心智模型：</b>把它理解成"容器的只读结构视图"
 */
public interface BeanInspector {

    /**
     * 检查是否包含指定名称的Bean
     *
     * @param beanName Bean名称（可以是别名）
     * @return 如果存在返回true，否则返回false
     */
    boolean containsBean(@NotBlank String beanName);

    /**
     * 检查是否包含指定名称的Bean定义
     *
     * @param beanName Bean名称
     * @return 如果存在返回true，否则返回false
     */
    boolean containsBeanDefinition(@NotBlank String beanName);

    /**
     * 获取指定名称的Bean定义
     *
     * @param beanName Bean名称
     * @return Bean定义
     * @throws BeanException 如果Bean定义不存在
     */
    @NotNull BeanDefinition getBeanDefinition(@NotBlank String beanName) throws BeanException;

    /**
     * 获取所有Bean定义的名称
     *
     * @return Bean定义名称数组，如果没有则返回空数组
     */
    @NotNull String[] getBeanDefinitionNames();

    /**
     * 获取Bean定义的总数
     *
     * @return Bean定义总数
     */
    int getBeanDefinitionCount();

    /**
     * 根据类型获取所有匹配的Bean名称
     *
     * @param type 目标类型
     * @return 匹配的Bean名称数组，如果没有则返回空数组
     */
    @NotNull String[] getBeanNamesForType(@NotNull Class<?> type);

    /**
     * 根据注解类型获取所有标注了该注解的Bean名称
     *
     * @param annotationType 注解类型
     * @return 标注了该注解的Bean名称数组，如果没有则返回空数组
     */
    @NotNull String[] getBeanNamesForAnnotation(@NotNull Class<? extends Annotation> annotationType);

    /**
     * 检查指定Bean是否为单例作用域
     *
     * @param beanName Bean名称
     * @return 如果是单例返回true，否则返回false
     * @throws BeanException 如果Bean不存在
     */
    boolean isSingleton(@NotBlank String beanName) throws BeanException;

    /**
     * 检查指定Bean是否为原型作用域
     *
     * @param beanName Bean名称
     * @return 如果是原型返回true，否则返回false
     * @throws BeanException 如果Bean不存在
     */
    boolean isPrototype(@NotBlank String beanName) throws BeanException;

    /**
     * 检查指定Bean是否匹配目标类型
     *
     * @param beanName Bean名称
     * @param targetType 目标类型
     * @return 如果匹配返回true，否则返回false
     * @throws BeanException 如果Bean不存在
     */
    boolean isTypeMatch(@NotBlank String beanName, @NotNull Class<?> targetType) throws BeanException;

    /**
     * 获取指定Bean的类型
     *
     * @param beanName Bean名称
     * @return Bean类型，如果Bean不存在返回null
     */
    @Nullable Class<?> getType(@NotBlank String beanName);

    /**
     * 获取指定Bean的所有别名
     *
     * @param beanName Bean名称
     * @return 别名数组，如果没有则返回空数组
     */
    @NotNull String[] getAliases(@NotBlank String beanName);

    /**
     * 在指定Bean上查找注解
     *
     * @param beanName Bean名称
     * @param annotationType 注解类型
     * @param <A> 注解类型参数
     * @return 注解实例，如果不存在返回null
     * @throws BeanException 如果Bean不存在
     */
    <A extends Annotation> @Nullable A findAnnotationOnBean(@NotNull String beanName, @NotNull Class<A> annotationType) throws BeanException;
}