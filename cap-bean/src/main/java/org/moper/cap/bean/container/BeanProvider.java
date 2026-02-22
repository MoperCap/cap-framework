package org.moper.cap.bean.container;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.moper.cap.bean.exception.BeanException;

/**
 * Bean提供者接口
 *
 * <p><b>核心语义：只负责"获取Bean实例"的能力</b>
 *
 * <p>此接口是IoC容器对外暴露的最小能力集，只包含获取Bean的方法。
 * 它不负责：
 * <ul>
 *   <li>注册Bean</li>
 *   <li>修改BeanDefinition</li>
 *   <li>生命周期管理</li>
 * </ul>
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>用于运行期访问</li>
 *   <li>对外暴露最小能力</li>
 *   <li>支持阶段裁剪</li>
 * </ul>
 *
 * <p><b>心智模型：</b>把它理解成"一个只读的Bean获取网关"
 *
 * <p><b>与Spring的对应关系：</b>
 * <ul>
 *   <li>对应Spring的BeanFactory接口</li>
 *   <li>但语义更纯粹，只包含getBean()相关方法</li>
 * </ul>
 */
public interface BeanProvider {

    /**
     * 根据Bean名称获取Bean实例
     *
     * @param beanName Bean名称，不能为空
     * @return Bean实例，永不为null
     * @throws BeanException 如果Bean不存在或获取失败
     */
    @NotNull Object getBean(@NotBlank String beanName) throws BeanException;

    /**
     * 根据Bean名称和类型获取Bean实例
     *
     * <p>此方法会进行类型检查，确保返回的Bean可以转换为requiredType。
     *
     * @param beanName Bean名称，不能为空
     * @param requiredType 要求的类型，不能为null
     * @param <T> 类型参数
     * @return Bean实例，永不为null
     * @throws BeanException 如果Bean不存在、类型不匹配或获取失败
     */
    <T> @NotNull T getBean(@NotBlank String beanName, @NotNull Class<T> requiredType) throws BeanException;

    /**
     * 根据类型获取Bean实例
     *
     * <p>如果存在多个匹配类型的Bean：
     * <ul>
     *   <li>如果有且仅有一个标记为primary的Bean，返回该Bean</li>
     *   <li>否则抛出NoUniqueBeanDefinitionException</li>
     * </ul>
     *
     * @param requiredType 要求的类型，不能为null
     * @param <T> 类型参数
     * @return Bean实例，永不为null
     * @throws BeanException 如果Bean不存在、存在多个且无primary标记或获取失败
     */
    <T> @NotNull T getBean(@NotNull Class<T> requiredType) throws BeanException;
}