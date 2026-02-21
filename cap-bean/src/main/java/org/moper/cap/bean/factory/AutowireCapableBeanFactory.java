package org.moper.cap.bean.factory;

import jakarta.validation.constraints.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 支持自动装配的Bean工厂接口
 * 提供依赖注入和Bean生命周期管理能力
 */
public interface AutowireCapableBeanFactory {
    /**
     * 创建Bean实例并完成自动装配
     *
     * @param beanClass Bean类型
     * @param <T> 类型参数
     * @return Bean实例
     * @throws BeanException 若创建或装配失败
     */
    <T> @NotNull T createBean(@NotNull Class<T> beanClass) throws BeanException;

    /**
     * 对已存在的Bean实例进行自动装配
     *
     * @param existingBean 已存在的Bean实例
     * @throws BeanException 若装配失败
     */
    void autowireBean(@NotNull Object existingBean) throws BeanException;

    /**
     * 配置Bean实例（包括依赖注入和初始化）
     *
     * @param existingBean 已存在的Bean实例
     * @param beanName Bean名称
     * @return 配置后的Bean实例（可能是原实例或代理实例）
     * @throws BeanException 若配置失败
     */
    @NotNull Object configureBean(@NotNull Object existingBean, @NotNull String beanName) throws BeanException;

    /**
     * 初始化Bean实例（调用初始化方法和后置处理器）
     *
     * @param existingBean 已存在的Bean实例
     * @param beanName Bean名称
     * @return 初始化后的Bean实例（可能是原实例或代理实例）
     * @throws BeanException 若初始化失败
     */
    @NotNull Object initializeBean(@NotNull Object existingBean, @NotNull String beanName) throws BeanException;

    /**
     * 应用Bean后置处理器的初始化前方法
     *
     * @param existingBean 已存在的Bean实例
     * @param beanName Bean名称
     * @return 处理后的Bean实例
     * @throws BeanException 若处理失败
     */
    @NotNull Object applyBeanPostProcessorsBeforeInitialization(@NotNull Object existingBean, @NotNull String beanName) throws BeanException;

    /**
     * 应用Bean后置处理器的初始化后方法
     *
     * @param existingBean 已存在的Bean实例
     * @param beanName Bean名称
     * @return 处理后的Bean实例
     * @throws BeanException 若处理失败
     */
    @NotNull Object applyBeanPostProcessorsAfterInitialization(@NotNull Object existingBean, @NotNull String beanName) throws BeanException;

    /**
     * 销毁Bean实例
     *
     * @param existingBean 已存在的Bean实例
     * @throws BeanException 若销毁失败
     */
    void destroyBean(@NotNull Object existingBean) throws BeanException;

    /**
     * 解析依赖
     *
     * @param dependencyType 依赖类型
     * @param requestingBeanName 请求的Bean名称（可选，用于循环依赖检测）
     * @return 解析的依赖实例，若无法解析则返回null
     * @throws BeanException 若解析失败
     */
    @Nullable Object resolveDependency(@NotNull Class<?> dependencyType, @Nullable String requestingBeanName) throws BeanException;
}
