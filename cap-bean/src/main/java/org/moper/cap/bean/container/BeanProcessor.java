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
 * <p><b>核心语义：管理Bean生命周期拦截阶段的调度</b>
 *
 * <p>此接口负责：
 * <ul>
 *   <li>维护并按 {@code order} 排序的 {@link BeanInterceptor} 列表</li>
 *   <li>在Bean生命周期各阶段依次调度拦截器链</li>
 *   <li>调用Bean自身的生命周期回调方法（init/destroy）</li>
 * </ul>
 *
 * <p><b>注意：此接口不涉及任何 Aware 机制，cap-bean 不支持 Aware。</b>
 */
public interface BeanProcessor {

    // ==================== 拦截器管理 ====================

    /**
     * 添加Bean拦截器
     *
     * @param interceptor Bean拦截器，不能为null
     */
    void addBeanInterceptor(@NotNull BeanInterceptor interceptor);

    /**
     * 获取所有已注册的Bean拦截器（只读视图，按 order 升序排列）
     *
     * @return 拦截器列表
     */
    @NotNull List<BeanInterceptor> getBeanInterceptors();

    /**
     * 获取Bean拦截器的数量
     *
     * @return 拦截器数量
     */
    int getBeanInterceptorCount();

    // ==================== 拦截器链调度 ====================

    /**
     * 应用 beforeInstantiation 拦截器链
     *
     * <p>如果链中某个拦截器返回非null，则短路后续拦截器，直接返回该对象。
     *
     * @param definition Bean定义，不能为null
     * @return 如果某个拦截器返回非null，则返回该对象；否则返回null
     * @throws BeanException 如果拦截处理失败
     */
    @Nullable Object applyBeforeInstantiation(@NotNull BeanDefinition definition) throws BeanException;

    /**
     * 应用 afterInstantiation 拦截器链
     *
     * @param bean       Bean实例，不能为null
     * @param definition Bean定义，不能为null
     * @return 处理后的Bean实例，永不为null
     * @throws BeanException 如果拦截处理失败
     */
    @NotNull Object applyAfterInstantiation(@NotNull Object bean, @NotNull BeanDefinition definition) throws BeanException;

    /**
     * 应用 afterPropertyInjection 拦截器链
     *
     * @param bean       Bean实例，不能为null
     * @param definition Bean定义，不能为null
     * @return 处理后的Bean实例，永不为null
     * @throws BeanException 如果拦截处理失败
     */
    @NotNull Object applyAfterPropertyInjection(@NotNull Object bean, @NotNull BeanDefinition definition) throws BeanException;

    /**
     * 应用 beforeInitialization 拦截器链
     *
     * @param bean       Bean实例，不能为null
     * @param beanName   Bean名称，不能为空
     * @param definition Bean定义，不能为null
     * @return 处理后的Bean实例，永不为null
     * @throws BeanException 如果拦截处理失败
     */
    @NotNull Object applyBeforeInitialization(@NotNull Object bean, @NotBlank String beanName, @NotNull BeanDefinition definition) throws BeanException;

    /**
     * 应用 afterInitialization 拦截器链
     *
     * @param bean       Bean实例，不能为null
     * @param beanName   Bean名称，不能为空
     * @param definition Bean定义，不能为null
     * @return 处理后的Bean实例，永不为null
     * @throws BeanException 如果拦截处理失败
     */
    @NotNull Object applyAfterInitialization(@NotNull Object bean, @NotBlank String beanName, @NotNull BeanDefinition definition) throws BeanException;

    // ==================== 生命周期方法调用 ====================

    /**
     * 调用Bean的初始化回调
     *
     * <p>调用顺序：
     * <ol>
     *   <li>如果Bean实现了 {@link org.moper.cap.bean.lifecycle.BeanInitializable}，
     *       则调用 {@code init()}</li>
     *   <li>如果 {@code BeanDefinition.initMethodName()} 不为null，则通过反射调用该方法</li>
     * </ol>
     *
     * @param beanName       Bean名称，不能为空
     * @param initMethodName 自定义init方法名（来自BeanDefinition，可以为null）
     * @param bean           Bean实例，不能为null
     * @throws BeanException 如果初始化失败
     */
    void invokeInitCallbacks(@NotBlank String beanName, @Nullable String initMethodName, @NotNull Object bean) throws BeanException;

    /**
     * 调用Bean的销毁回调
     *
     * <p>调用顺序：
     * <ol>
     *   <li>如果Bean实现了 {@link org.moper.cap.bean.lifecycle.BeanDisposable}，
     *       则调用 {@code destroy()}</li>
     *   <li>如果 {@code BeanDefinition.destroyMethodName()} 不为null，则通过反射调用该方法</li>
     * </ol>
     *
     * @param beanName          Bean名称，不能为空
     * @param destroyMethodName 自定义destroy方法名（来自BeanDefinition，可以为null）
     * @param bean              Bean实例，不能为null
     * @throws BeanException 如果销毁失败
     */
    void invokeDestroyCallbacks(@NotBlank String beanName, @Nullable String destroyMethodName, @NotNull Object bean) throws BeanException;
}