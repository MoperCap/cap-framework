package org.moper.cap.bean.container;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.moper.cap.bean.exception.BeanCreationException;
import org.moper.cap.bean.exception.BeanDestructionException;
import org.moper.cap.bean.interceptor.BeanInterceptor;

import java.util.List;

/**
 * Bean 容器接口（IoC 内核的完整能力聚合）。
 *
 * <p>聚合了三个子能力接口：
 * <pre>
 * BeanProvider   —— 获取 Bean 实例（读）
 * BeanInspector  —— 查询容器结构（读）
 * BeanRegistry   —— 注册 / 修改容器结构（写）
 * </pre>
 *
 * <p><b>职责边界：</b>
 * {@code BeanContainer} 只表达"容器能做什么"，是纯粹的能力集合，不编排任何生命周期。
 * 具体的生命周期编排（注册阶段的配置冻结、启动阶段的预实例化、关闭阶段的销毁）
 * 由持有此接口的上层组件负责：
 * <ul>
 *   <li>{@code BootstrapContext}：持有容器的写入能力，负责注册阶段的编排与冻结</li>
 *   <li>{@code ApplicationContext}：持有容器的读取能力，负责运行期获取与关闭时销毁</li>
 * </ul>
 *
 * <p><b>关于拦截器注册：</b>
 * {@link #addBeanInterceptor} 属于容器的内置配置能力，由 {@code BootstrapContext}
 * 在启动阶段调用，不属于 {@link BeanRegistry} 的 BeanDefinition 管理范畴，
 * 因此单独定义在此接口。
 */
public interface BeanContainer extends BeanProvider, BeanInspector, BeanRegistry {

    /**
     * 注册一个 {@link BeanInterceptor}。
     *
     * <p>多个拦截器按 {@link BeanInterceptor#getOrder()} 升序执行，值越小优先级越高。
     * 应在容器启动阶段（配置冻结前）完成所有拦截器的注册，
     * 具体的时机控制由 {@code BootstrapContext} 负责。
     *
     * @param interceptor 不能为 null
     */
    void addBeanInterceptor(BeanInterceptor interceptor);

    /**
     * 获取所有已注册的拦截器（只读视图，已按 {@link BeanInterceptor#getOrder()} 升序排列）。
     *
     * @return 不可变的拦截器列表，永不为 null
     */
    @NotNull
    List<BeanInterceptor> getBeanInterceptors();

    /**
     * 获取已注册的拦截器数量。
     *
     * @return 拦截器数量，最小为 0
     */
    int getBeanInterceptorCount();

    /**
     * 预实例化所有非懒加载的单例 Bean。
     *
     * <p>使配置错误在启动阶段暴露，而非推迟到首次 {@code getBean} 时。
     * 由 {@code BootstrapContext} 在配置冻结后、构建 {@code ApplicationContext} 前调用。
     *
     * @throws BeanCreationException 如果任意 Bean 实例化失败
     */
    void preInstantiateSingletons() throws BeanCreationException;

    /**
     * 销毁指定的单例 Bean，触发其 destroy() 回调。
     *
     * <p>若 Bean 未注册为可销毁 Bean，则此方法为空操作。
     *
     * @throws BeanDestructionException 如果销毁失败
     */
    void destroyBean(String beanName) throws BeanDestructionException;

    /**
     * 销毁容器中所有已缓存的单例 Bean。
     *
     * <p>按 Bean 注册顺序的逆序，依次调用每个单例 Bean 的
     * {@link org.moper.cap.bean.lifecycle.BeanLifecycle#destroy()} 方法。
     * 由 {@code ApplicationContext} 在关闭时调用。
     *
     * @throws BeanDestructionException 如果任意 Bean 销毁失败
     */
    void destroySingletons() throws BeanDestructionException;
}