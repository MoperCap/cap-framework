package org.moper.cap.bean.container.impl;

import jakarta.validation.constraints.NotNull;
import org.jetbrains.annotations.Nullable;
import org.moper.cap.bean.container.BeanCreationEngine;
import org.moper.cap.bean.container.BeanProvider;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.definition.BeanScope;
import org.moper.cap.bean.definition.InstantiationPolicy;
import org.moper.cap.bean.exception.BeanCreationException;
import org.moper.cap.bean.exception.BeanDestructionException;
import org.moper.cap.bean.exception.BeanException;
import org.moper.cap.bean.exception.BeanInitializationException;
import org.moper.cap.bean.interceptor.BeanInterceptor;
import org.moper.cap.bean.lifecycle.BeanLifecycle;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * {@link BeanCreationEngine} 的默认实现。
 *
 * <p>整合了原 {@code BeanCreator} 与 {@code BeanProcessor} 的全部职责：
 * <ul>
 *   <li>维护有序的 {@link BeanInterceptor} 列表，在创建流程各阶段依次调度</li>
 *   <li>编排 Bean 的完整创建流程（实例化 → 属性注入 → 初始化）</li>
 *   <li>触发 {@link BeanLifecycle} 生命周期回调</li>
 *   <li>管理可销毁单例的注册与逆序销毁</li>
 * </ul>
 *
 * <p><b>线程安全说明：</b>
 * {@code disposableBeans} 仅在单例创建路径上写入，而单例创建在
 * {@link org.moper.cap.bean.container.impl.DefaultBeanContainer} 中已通过 {@code synchronized} 保证互斥，
 * 因此此处无需额外同步。
 */
public class DefaultBeanCreationEngine implements BeanCreationEngine {

    /**
     * 用于解析构造函数 / 工厂方法参数的依赖
     */
    private final @NotNull BeanProvider beanProvider;

    /**
     * 已注册的拦截器，按 {@link BeanInterceptor#getOrder()} 升序维护
     */
    private final @NotNull List<BeanInterceptor> interceptors = new ArrayList<>();

    /**
     * 可销毁单例列表，按注册顺序存储，销毁时逆序执行。
     * key = beanName，value = bean 实例
     */
    private final @NotNull LinkedHashMap<String, Object> disposableBeans = new LinkedHashMap<>();

    /**
     * 构造函数，注入 {@link BeanProvider} 以支持实例化阶段的依赖解析。
     * @param beanProvider BeanProvider 实例，不能为 null
     */
    public DefaultBeanCreationEngine(@NotNull BeanProvider beanProvider) {
        this.beanProvider = beanProvider;
    }

    /**
     * {@inheritDoc}
     *
     * <p>完整执行以下流程：
     * <ol>
     *   <li>{@code beforeInstantiation} 拦截器链（短路则跳至步骤 6）</li>
     *   <li>实例化</li>
     *   <li>{@code afterInstantiation} 拦截器链</li>
     *   <li>属性注入</li>
     *   <li>{@code afterPropertyInjection} 拦截器链</li>
     *   <li>{@code beforeInitialization} 拦截器链</li>
     *   <li>{@link BeanLifecycle#afterPropertiesSet()} 回调</li>
     *   <li>{@code afterInitialization} 拦截器链</li>
     *   <li>注册销毁回调</li>
     * </ol>
     */
    @Override
    public Object createBean(String beanName, BeanDefinition beanDefinition) throws BeanException {

        // 1. beforeInstantiation：拦截器有机会短路整个实例化流程
        Object shortCircuit = applyBeforeInstantiation(beanDefinition);
        if (shortCircuit != null) {
            // 短路只跳过"实例化"和"属性注入"，afterInstantiation 及后续拦截器链仍然执行
            Object instance = applyAfterInstantiation(shortCircuit, beanDefinition);
            instance = applyAfterPropertyInjection(instance, beanDefinition);
            instance = applyInitializationPhase(beanName, beanDefinition, instance);
            registerDisposableIfNeeded(beanName, beanDefinition, instance);
            return instance;
        }

        // 2. 实例化
        Object instance = instantiateBean(beanName, beanDefinition);

        // 3. afterInstantiation
        instance = applyAfterInstantiation(instance, beanDefinition);

        // 4. 属性注入（构造函数注入已在步骤 2 完成；字段/Setter 注入由上层拦截器实现）
        populateBean(beanName, beanDefinition, instance);

        // 5. afterPropertyInjection
        instance = applyAfterPropertyInjection(instance, beanDefinition);

        // 6-8. 初始化阶段
        instance = applyInitializationPhase(beanName, beanDefinition, instance);

        // 9. 注册销毁回调
        registerDisposableIfNeeded(beanName, beanDefinition, instance);

        return instance;
    }

    @Override
    public void destroyBean(String beanName) throws BeanDestructionException {
        Object bean = disposableBeans.remove(beanName);
        if (bean == null) {
            return; // 未注册为可销毁，空操作
        }
        invokeDestroyCallback(beanName, bean);
    }

    @Override
    public void destroyAllSingletons() throws BeanDestructionException {
        List<String> names = new ArrayList<>(disposableBeans.keySet());
        // 逆序销毁：后注册的 Bean 先销毁
        for (int i = names.size() - 1; i >= 0; i--) {
            destroyBean(names.get(i));
        }
        disposableBeans.clear();
    }

    @Override
    public void addBeanInterceptor(BeanInterceptor interceptor) {
        if (interceptor == null) {
            throw new IllegalArgumentException("BeanInterceptor must not be null");
        }
        interceptors.add(interceptor);
        interceptors.sort((a, b) -> Integer.compare(a.getOrder(), b.getOrder()));
    }

    @Override
    public List<BeanInterceptor> getBeanInterceptors() {
        return Collections.unmodifiableList(interceptors);
    }

    @Override
    public int getBeanInterceptorCount() {
        return interceptors.size();
    }

    /**
     * 根据 {@link BeanDefinition#instantiationPolicy()} 选择实例化策略并执行。
     */
    private Object instantiateBean(String beanName, BeanDefinition beanDefinition) throws BeanException {
        InstantiationPolicy policy = beanDefinition.instantiationPolicy();
        try {
            if (policy.isConstructor()) {
                return instantiateByConstructor(beanName, beanDefinition);
            } else if (policy.isStaticFactory()) {
                return instantiateByStaticFactory(beanName, beanDefinition);
            } else {
                return instantiateByInstanceFactory(beanName, beanDefinition);
            }
        } catch (BeanException e) {
            throw e; // BeanException 直接透传，不二次包装
        } catch (Exception e) {
            throw new BeanCreationException(beanName, "Instantiation failed", e);
        }
    }

    private Object instantiateByConstructor(String beanName, BeanDefinition beanDefinition) throws Exception {
        Class<?> type      = beanDefinition.type();
        Class<?>[] argTypes = beanDefinition.instantiationPolicy().argTypes();

        if (argTypes.length == 0) {
            return type.getDeclaredConstructor().newInstance();
        }
        Object[] args = resolveArguments(beanName, beanDefinition);
        Constructor<?> ctor = type.getDeclaredConstructor(argTypes);
        return ctor.newInstance(args);
    }

    private Object instantiateByStaticFactory(String beanName, BeanDefinition beanDefinition) throws Exception {
        InstantiationPolicy policy = beanDefinition.instantiationPolicy();
        Object[] args = resolveArguments(beanName, beanDefinition);
        Method method = beanDefinition.type()
                .getDeclaredMethod(policy.factoryMethodName(), policy.argTypes());
        return method.invoke(null, args);
    }

    private Object instantiateByInstanceFactory(String beanName, BeanDefinition beanDefinition) throws Exception {
        InstantiationPolicy policy  = beanDefinition.instantiationPolicy();
        Object factoryBean          = beanProvider.getBean(policy.factoryBeanName());
        Object[] args               = resolveArguments(beanName, beanDefinition);
        Method method = factoryBean.getClass()
                .getDeclaredMethod(policy.factoryMethodName(), policy.argTypes());
        return method.invoke(factoryBean, args);
    }

    /**
     * 属性注入占位。
     *
     * <p>构造函数注入已在实例化阶段完成。
     * 字段注入、Setter 注入等由上层 cap-context 通过 {@link BeanInterceptor} 实现，
     * cap-bean 内核不内置注解扫描能力。
     */
    private void populateBean(String beanName, BeanDefinition beanDefinition, Object beanInstance) {
        // 预留扩展点，当前无内核级注入逻辑
    }

    /**
     * 执行初始化阶段：
     * {@code beforeInitialization} 拦截器链
     * → {@link BeanLifecycle#afterPropertiesSet()}
     * → {@code afterInitialization} 拦截器链
     */
    private Object applyInitializationPhase(String beanName,
                                            BeanDefinition beanDefinition,
                                            Object beanInstance) throws BeanException {
        Object current = applyBeforeInitialization(beanInstance, beanName, beanDefinition);
        invokeInitCallback(beanName, current);
        current = applyAfterInitialization(current, beanName, beanDefinition);
        return current;
    }

    /**
     * 触发 {@link BeanLifecycle#afterPropertiesSet()}。
     *
     * <p>若 Bean 未实现 {@link BeanLifecycle}，此方法为空操作。
     *
     * @throws BeanInitializationException 如果回调执行失败
     */
    private void invokeInitCallback(String beanName, Object bean) throws BeanInitializationException {
        if (bean instanceof BeanLifecycle lifecycle) {
            try {
                lifecycle.afterPropertiesSet();
            } catch (Exception e) {
                throw new BeanInitializationException(beanName, e);
            }
        }
    }

    /**
     * 触发 {@link BeanLifecycle#destroy()}。
     *
     * <p>若 Bean 未实现 {@link BeanLifecycle}，此方法为空操作。
     *
     * @throws BeanDestructionException 如果回调执行失败
     */
    private void invokeDestroyCallback(String beanName, Object bean) throws BeanDestructionException {
        if (bean instanceof BeanLifecycle lifecycle) {
            try {
                lifecycle.destroy();
            } catch (Exception e) {
                throw new BeanDestructionException(beanName, e);
            }
        }
    }

    /**
     * 满足以下全部条件时将 Bean 注册到销毁列表：
     * <ul>
     *   <li>作用域为 {@link BeanScope#SINGLETON}</li>
     *   <li>实现了 {@link BeanLifecycle}</li>
     * </ul>
     */
    private void registerDisposableIfNeeded(String beanName,
                                            BeanDefinition beanDefinition,
                                            Object beanInstance) {
        if (beanDefinition.scope() == BeanScope.SINGLETON
                && beanInstance instanceof BeanLifecycle) {
            disposableBeans.put(beanName, beanInstance);
        }
    }

    /**
     * 根据 {@link InstantiationPolicy#argTypes()} 从容器解析参数值数组。
     *
     * <p>{@link BeanException} 直接透传，不再二次包装，保证根因可见。
     */
    private Object[] resolveArguments(String beanName, BeanDefinition beanDefinition) throws BeanException {
        Class<?>[] argTypes = beanDefinition.instantiationPolicy().argTypes();
        if (argTypes.length == 0) {
            return new Object[0];
        }
        Object[] args = new Object[argTypes.length];
        for (int i = 0; i < argTypes.length; i++) {
            // BeanException（含 NoSuchBeanDefinitionException）直接透传，保留精确根因
            args[i] = beanProvider.getBean(argTypes[i]);
        }
        return args;
    }

    /**
     * 调度 {@code beforeInstantiation} 拦截器链。
     *
     * <p>短路语义：链中若某个拦截器返回非 null，立即终止后续拦截器调用，
     * 以该对象短路整个实例化流程。
     *
     * @return 短路用的 Bean 实例；所有拦截器均返回 null 时返回 null
     */
    private @Nullable Object applyBeforeInstantiation(BeanDefinition definition) throws BeanException {
        for (BeanInterceptor interceptor : interceptors) {
            Object result = interceptor.beforeInstantiation(definition);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * 调度 {@code afterInstantiation} 拦截器链。
     *
     * <p>链式传递：前一个拦截器的返回值作为后一个拦截器的 {@code bean} 入参。
     */
    private Object applyAfterInstantiation(Object bean, BeanDefinition definition) throws BeanException {
        Object current = bean;
        for (BeanInterceptor interceptor : interceptors) {
            current = interceptor.afterInstantiation(current, definition);
        }
        return current;
    }

    /**
     * 调度 {@code afterPropertyInjection} 拦截器链。
     */
    private Object applyAfterPropertyInjection(Object bean, BeanDefinition definition) throws BeanException {
        Object current = bean;
        for (BeanInterceptor interceptor : interceptors) {
            current = interceptor.afterPropertyInjection(current, definition);
        }
        return current;
    }

    /**
     * 调度 {@code beforeInitialization} 拦截器链。
     */
    private Object applyBeforeInitialization(Object bean, String beanName, BeanDefinition definition) throws BeanException {
        Object current = bean;
        for (BeanInterceptor interceptor : interceptors) {
            current = interceptor.beforeInitialization(current, beanName, definition);
        }
        return current;
    }

    /**
     * 调度 {@code afterInitialization} 拦截器链。
     *
     * <p>AOP 代理的创建通常在此阶段完成，最终返回的对象可能是代理而非原始 Bean。
     */
    private Object applyAfterInitialization(Object bean, String beanName, BeanDefinition definition) throws BeanException {
        Object current = bean;
        for (BeanInterceptor interceptor : interceptors) {
            current = interceptor.afterInitialization(current, beanName, definition);
        }
        return current;
    }
}