package org.moper.cap.bean.lifecycle;

import org.moper.cap.bean.exception.BeanException;
import org.moper.cap.bean.interceptor.BeanInterceptor;

/**
 * Bean初始化回调接口
 *
 * <p><b>核心语义：Bean 在完成属性注入后，可自行执行初始化逻辑</b>
 *
 * <p>与 Spring 的 {@code InitializingBean} 对应，但方法命名更直观：
 * <ul>
 *   <li>Spring: {@code afterPropertiesSet()} —— 语义模糊，名字不直观</li>
 *   <li>Cap:    {@code init()} —— 语义清晰，意图明确</li>
 * </ul>
 *
 * <p><b>调用时机：</b>在 {@link BeanInterceptor#beforeInitialization} 之后、
 * {@link BeanInterceptor#afterInitialization} 之前。
 *
 * <p><b>调用顺序（在 initializeBean 内部）：</b>
 * <pre>
 * applyBeforeInitialization
 *   → BeanInitializable.init()        ← 此接口
 *   → 自定义 initMethodName（反射）
 * applyAfterInitialization
 * </pre>
 *
 * <p><b>设计原则（参考 Guice）：</b>
 * <ul>
 *   <li>不注入任何容器引用（无 Aware）</li>
 *   <li>只做业务初始化，不依赖框架内部对象</li>
 * </ul>
 */
public interface BeanInitializable {

    /**
     * 执行Bean的初始化逻辑
     *
     * <p>此方法在依赖注入完成后调用，可以在此处：
     * <ul>
     *   <li>校验注入的依赖是否合法</li>
     *   <li>建立连接、预热缓存等资源初始化</li>
     *   <li>基于注入的配置完成内部状态初始化</li>
     * </ul>
     *
     * @throws BeanException 如果初始化失败
     */
    void init() throws BeanException;
}