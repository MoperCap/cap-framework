package org.moper.cap.bean.container;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.exception.BeanException;

/**
 * Bean创建器接口（内部SPI）
 *
 * <p><b>核心语义：负责Bean的创建流程</b>
 *
 * <p><b>创建流程：</b>
 * <pre>
 * 1. 实例化 (instantiate)
 * 2. 属性填充 (populate)
 * 3. 初始化 (initialize)
 * 4. 注册销毁回调 (registerDisposable)
 * </pre>
 *
 * <p><b>心智模型：</b>把它理解成"Bean的工厂车间"
 */
public interface BeanCreator {

    /**
     * 创建Bean实例（完整流程）
     *
     * <p>此方法会执行完整的Bean创建流程：
     * <ol>
     *   <li>实例化Bean</li>
     *   <li>填充属性（依赖注入）</li>
     *   <li>初始化Bean</li>
     *   <li>注册销毁回调（如果是单例）</li>
     * </ol>
     *
     * @param beanName Bean名称
     * @param beanDefinition Bean定义
     * @return 创建并初始化完成的Bean实例
     * @throws BeanException 如果创建失败
     */
    @NotNull Object createBean(@NotBlank String beanName, @NotNull BeanDefinition beanDefinition) throws BeanException;

    /**
     * 实例化Bean（仅创建实例，不注入依赖）
     *
     * @param beanName Bean名称
     * @param beanDefinition Bean定义
     * @return Bean实例（未注入依赖，未初始化）
     * @throws BeanException 如果实例化失败
     */
    @NotNull Object instantiateBean(@NotBlank String beanName, @NotNull BeanDefinition beanDefinition) throws BeanException;

    /**
     * 填充Bean属性（依赖注入）
     *
     * @param beanName Bean名称
     * @param beanDefinition Bean定义
     * @param beanInstance Bean实例
     * @throws BeanException 如果属性填充失败
     */
    void populateBean(@NotBlank String beanName, @NotNull BeanDefinition beanDefinition, @NotNull Object beanInstance) throws BeanException;

    /**
     * 初始化Bean（执行生命周期回调）
     *
     * @param beanName Bean名称
     * @param beanDefinition Bean定义
     * @param beanInstance Bean实例
     * @return 初始化后的Bean实例（可能是原实例或代理实例）
     * @throws BeanException 如果初始化失败
     */
    @NotNull Object initializeBean(@NotBlank String beanName, @NotNull BeanDefinition beanDefinition, @NotNull Object beanInstance) throws BeanException;

    /**
     * 注册可销毁的Bean
     *
     * @param beanName Bean名称
     * @param beanDefinition Bean定义
     * @param beanInstance Bean实例
     */
    void registerDisposableBean(@NotBlank String beanName, @NotNull BeanDefinition beanDefinition, @NotNull Object beanInstance);

    /**
     * 解析构造函数/工厂方法的参数
     *
     * @param beanName Bean名称（用于循环依赖检测）
     * @param parameterTypes 参数类型数组
     * @return 解析后的参数值数组
     * @throws BeanException 如果解析失败或依赖不存在
     */
    @NotNull Object[] resolveConstructorArguments(@NotBlank String beanName, @NotNull Class<?>[] parameterTypes) throws BeanException;
}