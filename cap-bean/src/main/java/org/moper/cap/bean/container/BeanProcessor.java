package org.moper.cap.bean.container;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jetbrains.annotations.Nullable;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.exception.BeanException;
import org.moper.cap.bean.interceptor.BeanInterceptor;

import java.util.List;

/**
 * Bean处理器管理接口（内部SPI）
 *
 * <p><b>核心语义：管理Bean的初始化和销毁阶段，以及拦截器调度</b>
 */
public interface BeanProcessor {

    /**
     * 添加Bean拦截器
     *
     * @param interceptor Bean拦截器
     */
    void addBeanInterceptor(@NotNull BeanInterceptor interceptor);

    /**
     * 获取所有已注册的Bean拦截器
     *
     * @return 拦截器列表（只读视图，按order排序）
     */
    @NotNull List<BeanInterceptor> getBeanInterceptors();

    /**
     * 获取Bean拦截器的数量
     *
     * @return 拦截器数量
     */
    int getBeanInterceptorCount();

    /**
     * 应用beforeInstantiation拦截器
     *
     * @param definition Bean定义
     * @return 如果某个拦截器返回非null，则返回该对象；否则返回null
     * @throws BeanException 如果拦截处理失败
     */
    @Nullable Object applyBeforeInstantiation(@NotNull BeanDefinition definition) throws BeanException;

    /**
     * 应用afterInstantiation拦截器
     *
     * @param bean Bean实例
     * @param definition Bean定义
     * @return 处理后的Bean实例
     * @throws BeanException 如果拦截处理失败
     */
    @NotNull Object applyAfterInstantiation(@NotNull Object bean, @NotNull BeanDefinition definition) throws BeanException;

    /**
     * 应用afterPropertyInjection拦截器
     *
     * @param bean Bean实例
     * @param definition Bean定义
     * @return 处理后的Bean实例
     * @throws BeanException 如果拦截处理失败
     */
    @NotNull Object applyAfterPropertyInjection(@NotNull Object bean, @NotNull BeanDefinition definition) throws BeanException;

    /**
     * 应用beforeInitialization拦截器
     *
     * @param bean Bean实例
     * @param beanName Bean名称
     * @param definition Bean定义
     * @return 处理后的Bean实例
     * @throws BeanException 如果拦截处理失败
     */
    @NotNull Object applyBeforeInitialization(@NotNull Object bean, @NotBlank String beanName, @NotNull BeanDefinition definition) throws BeanException;

    /**
     * 应用afterInitialization拦截器
     *
     * @param bean Bean实例
     * @param beanName Bean名称
     * @param definition Bean定义
     * @return 处理后的Bean实例
     * @throws BeanException 如果拦截处理失败
     */
    @NotNull Object applyAfterInitialization(@NotNull Object bean, @NotBlank String beanName, @NotNull BeanDefinition definition) throws BeanException;

    /**
     * 调用Aware接口方法
     *
     * @param beanName Bean名称
     * @param bean Bean实例
     * @throws BeanException 如果回调失败
     */
    void invokeAwareMethods(@NotBlank String beanName, @NotNull Object bean) throws BeanException;

    /**
     * 调用Bean的初始化方法
     *
     * @param beanName Bean名称
     * @param initMethodName 自定义init方法名（可以为null）
     * @param bean Bean实例
     * @throws BeanException 如果初始化失败
     */
    void invokeInitMethods(@NotBlank String beanName, @Nullable String initMethodName, @NotNull Object bean) throws BeanException;

    /**
     * 调用Bean的销毁方法
     *
     * @param beanName Bean名称
     * @param destroyMethodName 自定义destroy方法名（可以为null）
     * @param bean Bean实例
     * @throws BeanException 如果销毁失败
     */
    void invokeDestroyMethods(@NotBlank String beanName, @Nullable String destroyMethodName, @NotNull Object bean) throws BeanException;
}