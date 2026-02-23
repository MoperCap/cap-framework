package org.moper.cap.bean.container;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.exception.BeanDestructionException;
import org.moper.cap.bean.exception.BeanException;
import org.moper.cap.bean.interceptor.BeanInterceptor;

import java.util.List;

/**
 * Bean 创建引擎接口（内部 SPI）。
 *
 * <p><b>核心语义：Bean 完整生命周期（创建 → 销毁）的执行引擎</b>
 *
 * <p>封装了以下两类内部职责，对 {@link BeanContainer} 只暴露必要的操作入口：
 * <ul>
 *   <li><b>创建流程编排</b>：实例化 → 属性注入 → 拦截器链 → 生命周期回调</li>
 *   <li><b>拦截器管理</b>：维护有序的 {@link BeanInterceptor} 列表并在各阶段调度</li>
 * </ul>
 *
 * <p><b>完整创建流程：</b>
 * <pre>
 * createBean()
 *   ├─ beforeInstantiation 拦截器链
 *   │    └─ 若返回非 null：短路，直接进入初始化阶段
 *   ├─ 实例化（构造函数 / 静态工厂 / 实例工厂）
 *   ├─ afterInstantiation 拦截器链
 *   ├─ 属性注入
 *   ├─ afterPropertyInjection 拦截器链
 *   ├─ beforeInitialization 拦截器链
 *   ├─ BeanLifecycle.afterPropertiesSet()
 *   ├─ afterInitialization 拦截器链
 *   └─ 注册销毁回调（单例 + 实现了 BeanLifecycle）
 * </pre>
 *
 * @see BeanInterceptor
 * @see org.moper.cap.bean.lifecycle.BeanLifecycle
 */
public interface BeanCreationEngine {

    /**
     * 执行完整的 Bean 创建流程。
     *
     * @param beanName       Bean 名称，不能为空
     * @param beanDefinition Bean 定义，不能为 null
     * @return 创建并初始化完成的 Bean 实例（可能是代理对象）
     * @throws BeanException 如果创建过程中任意阶段失败
     */
    @NotNull Object createBean(@NotBlank String beanName, @NotNull BeanDefinition beanDefinition) throws BeanException;

    /**
     * 销毁指定的单例 Bean，触发其
     * {@link org.moper.cap.bean.lifecycle.BeanLifecycle#destroy()} 回调。
     *
     * <p>若 Bean 未注册为可销毁 Bean，则此方法为空操作。
     *
     * @param beanName Bean 名称，不能为空
     * @throws BeanDestructionException 如果 {@code destroy()} 执行失败
     */
    void destroyBean(@NotBlank String beanName) throws BeanDestructionException;

    /**
     * 销毁所有已注册的可销毁单例 Bean。
     *
     * <p>按注册顺序的逆序依次调用 {@link #destroyBean}，
     * 由 {@link BeanContainer#destroySingletons()} 触发。
     *
     * @throws BeanDestructionException 如果任意 Bean 销毁失败
     */
    void destroyAllSingletons() throws BeanDestructionException;

    /**
     * 注册一个 {@link BeanInterceptor}，内部按 {@link BeanInterceptor#getOrder()} 升序排列。
     *
     * <p>应在容器启动阶段（配置冻结前）完成所有拦截器的注册。
     *
     * @param interceptor 不能为 null
     * @throws IllegalArgumentException 如果 interceptor 为 null
     */
    void addBeanInterceptor(@NotNull BeanInterceptor interceptor);

    /**
     * 获取所有已注册的拦截器（只读视图，已按 {@link BeanInterceptor#getOrder()} 升序排列）。
     *
     * @return 不可变的拦截器列表，永不为 null
     */
    @NotNull List<BeanInterceptor> getBeanInterceptors();

    /**
     * 获取已注册的拦截器数量。
     *
     * @return 拦截器数量，最小为 0
     */
    int getBeanInterceptorCount();
}