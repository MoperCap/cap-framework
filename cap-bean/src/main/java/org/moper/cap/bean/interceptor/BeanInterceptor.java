package org.moper.cap.bean.interceptor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jetbrains.annotations.Nullable;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.exception.BeanException;

/**
 * Bean 生命周期拦截器接口。
 *
 * <p><b>核心语义：在 Bean 创建过程的各阶段插入横切逻辑</b>
 *
 * <p>所有方法均有默认实现，只需重写关心的拦截点。
 * 多个拦截器按 {@link #getOrder()} 升序依次执行。
 *
 * <p><b>与 {@link org.moper.cap.bean.lifecycle.BeanLifecycle} 的职责区分：</b>
 * <ul>
 *   <li>{@code BeanInterceptor}：面向框架扩展者，处理横切关注点
 *       （AOP 代理创建、注解处理、依赖验证等）</li>
 *   <li>{@code BeanLifecycle}：面向普通用户，处理 Bean 自身的业务初始化与资源释放</li>
 * </ul>
 *
 * <p><b>完整的 Bean 创建流程：</b>
 * <pre>
 * 1. beforeInstantiation
 *    ├─ 若返回非 null：短路，跳过步骤 2-4，直接进入步骤 5
 *    └─ 若返回 null：继续正常流程
 *
 * 2. [容器执行实例化（构造函数 / 工厂方法）]
 *
 * 3. afterInstantiation
 *
 * 4. [容器执行属性注入]
 *
 * 5. afterPropertyInjection
 *
 * 6. beforeInitialization
 *
 * 7. [容器调用 BeanLifecycle.afterPropertiesSet()]
 *
 * 8. afterInitialization
 *
 * 9. Bean 就绪
 * </pre>
 */
public interface BeanInterceptor {

    /**
     * 在 Bean 实例化之前调用。
     *
     * <p>若返回非 null，以该对象作为 Bean 实例，跳过实例化和属性注入，
     * 直接进入 {@link #afterPropertyInjection} 阶段。
     *
     * @param definition Bean 定义，不能为 null
     * @return 短路用的 Bean 实例；返回 null 则继续正常流程
     * @throws BeanException 处理失败
     */
    default @Nullable Object beforeInstantiation(
            @NotNull BeanDefinition definition) throws BeanException {
        return null;
    }

    /**
     * 在 Bean 实例化之后、属性注入之前调用。
     *
     * @param bean       刚创建的 Bean 实例，不能为 null
     * @param definition Bean 定义，不能为 null
     * @return 要继续使用的 Bean 实例，不能为 null
     * @throws BeanException 处理失败
     */
    default @NotNull Object afterInstantiation(
            @NotNull Object bean,
            @NotNull BeanDefinition definition) throws BeanException {
        return bean;
    }

    /**
     * 在属性注入完成之后、初始化回调之前调用。
     *
     * @param bean       已完成属性注入的 Bean 实例，不能为 null
     * @param definition Bean 定义，不能为 null
     * @return 要继续使用的 Bean 实例，不能为 null
     * @throws BeanException 处理失败
     */
    default @NotNull Object afterPropertyInjection(
            @NotNull Object bean,
            @NotNull BeanDefinition definition) throws BeanException {
        return bean;
    }

    /**
     * 在 {@link org.moper.cap.bean.lifecycle.BeanLifecycle#afterPropertiesSet()} 之前调用。
     *
     * @param bean       Bean 实例，不能为 null
     * @param beanName   Bean 名称，不能为空
     * @param definition Bean 定义，不能为 null
     * @return 要继续使用的 Bean 实例，不能为 null
     * @throws BeanException 处理失败
     */
    default @NotNull Object beforeInitialization(
            @NotNull Object bean,
            @NotBlank String beanName,
            @NotNull BeanDefinition definition) throws BeanException {
        return bean;
    }

    /**
     * 在所有初始化回调完成之后调用，这是修改 Bean 的最后机会。
     *
     * <p>AOP 代理的创建通常在此阶段完成。
     *
     * @param bean       已完全初始化的 Bean 实例，不能为 null
     * @param beanName   Bean 名称，不能为空
     * @param definition Bean 定义，不能为 null
     * @return 最终暴露给外部的 Bean 实例（可以是代理），不能为 null
     * @throws BeanException 处理失败
     */
    default @NotNull Object afterInitialization(
            @NotNull Object bean,
            @NotBlank String beanName,
            @NotNull BeanDefinition definition) throws BeanException {
        return bean;
    }

    /**
     * 拦截器执行顺序，值越小优先级越高。
     *
     * <p><b>建议范围：</b>
     * <ul>
     *   <li>框架内置拦截器：{@code Integer.MIN_VALUE} ~ {@code -100}</li>
     *   <li>框架扩展拦截器：{@code -100} ~ {@code 0}</li>
     *   <li>用户自定义拦截器：{@code 0} ~ {@code Integer.MAX_VALUE}</li>
     * </ul>
     */
    default int getOrder() {
        return 0;
    }
}