package org.moper.cap.bean.interceptor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jetbrains.annotations.Nullable;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.exception.BeanException;

/**
 * Bean拦截器接口
 *
 * <p><b>核心设计理念：阶段拦截而非后置处理</b>
 *
 * <p>与Spring的BeanPostProcessor不同，此接口提供了更细粒度的生命周期拦截点：
 * <ul>
 *   <li><b>实例化前：</b>可以返回代理对象，跳过实例化</li>
 *   <li><b>实例化后：</b>可以修改实例或返回包装对象</li>
 *   <li><b>属性注入后：</b>可以执行额外的注入或验证</li>
 *   <li><b>初始化前：</b>可以修改Bean状态</li>
 *   <li><b>初始化后：</b>可以创建代理（AOP）</li>
 * </ul>
 *
 * <p><b>完整的Bean创建流程：</b>
 * <pre>
 * 1. beforeInstantiation      (实例化前拦截)
 *    ├─ 如果返回非null，跳过2-6，直接到7
 *    └─ 如果返回null，继续正常流程
 *
 * 2. 实例化Bean                (通过构造函数/工厂方法)
 *
 * 3. afterInstantiation        (实例化后拦截)
 *    └─ 可以替换实例
 *
 * 4. 属性填充                  (依赖注入)
 *
 * 5. afterPropertyInjection    (属性注入后拦截)
 *    └─ 可以执行额外注入或验证
 *
 * 6. Aware接口回调
 *
 * 7. beforeInitialization      (初始化前拦截)
 *    └─ 可以修改Bean状态
 *
 * 8. InitializingBean.afterPropertiesSet
 *
 * 9. 自定义init方法
 *
 * 10. afterInitialization      (初始化后拦截)
 *     └─ 可以创建AOP代理
 *
 * 11. Bean完成
 * </pre>
 *
 * <p><b>与Spring BeanPostProcessor的区别：</b>
 * <table border="1">
 *   <tr>
 *     <th>特性</th>
 *     <th>Spring BeanPostProcessor</th>
 *     <th>BeanInterceptor</th>
 *   </tr>
 *   <tr>
 *     <td>拦截点数量</td>
 *     <td>2个（before/after初始化）</td>
 *     <td>5个（覆盖整个生命周期）</td>
 *   </tr>
 *   <tr>
 *     <td>实例化控制</td>
 *     <td>不支持</td>
 *     <td>支持（beforeInstantiation）</td>
 *   </tr>
 *   <tr>
 *     <td>属性注入拦截</td>
 *     <td>不支持</td>
 *     <td>支持（afterPropertyInjection）</td>
 *   </tr>
 *   <tr>
 *     <td>BeanDefinition访问</td>
 *     <td>不支持</td>
 *     <td>所有阶段都可访问</td>
 *   </tr>
 * </table>
 *
 * <p><b>使用场景示例：</b>
 * <ul>
 *   <li><b>beforeInstantiation：</b>返回缓存的代理对象，避免重复创建</li>
 *   <li><b>afterInstantiation：</b>对特定类型的Bean进行包装</li>
 *   <li><b>afterPropertyInjection：</b>验证必需属性已注入</li>
 *   <li><b>beforeInitialization：</b>设置默认值或执行预处理</li>
 *   <li><b>afterInitialization：</b>创建AOP代理、注册到其他系统</li>
 * </ul>
 *
 * <p><b>默认方法：</b>所有方法都是默认方法，只需实现关心的拦截点。
 */
public interface BeanInterceptor {

    /**
     * 在Bean实例化之前调用
     *
     * <p><b>作用：</b>
     * <ul>
     *   <li>可以返回一个代理对象，完全跳过容器的实例化流程</li>
     *   <li>可以从缓存中返回Bean，避免重复创建</li>
     *   <li>可以基于BeanDefinition决定是否需要特殊处理</li>
     * </ul>
     *
     * <p><b>如果返回非null：</b>
     * <ul>
     *   <li>跳过实例化、属性注入、Aware回调</li>
     *   <li>直接进入beforeInitialization阶段</li>
     *   <li>适用于完全自定义的Bean创建</li>
     * </ul>
     *
     * <p><b>使用示例：</b>
     * <pre>{@code
     * @Override
     * public Object beforeInstantiation(BeanDefinition definition) throws BeanException {
     *     // 如果是特定类型，返回缓存的代理
     *     if (definition.type() == MyService.class) {
     *         return cachedProxy;
     *     }
     *     return null; // 继续正常流程
     * }
     * }</pre>
     *
     * @param definition Bean定义，永不为null
     * @return 如果返回非null，则使用此对象作为Bean实例；如果返回null，继续正常实例化流程
     * @throws BeanException 如果拦截处理失败
     */
    default @Nullable Object beforeInstantiation(@NotNull BeanDefinition definition) throws BeanException {
        return null;
    }

    /**
     * 在Bean实例化之后、属性注入之前调用
     *
     * <p><b>作用：</b>
     * <ul>
     *   <li>可以替换实例（返回包装对象）</li>
     *   <li>可以对实例进行检查或标记</li>
     *   <li>可以执行实例级别的后处理</li>
     * </ul>
     *
     * <p><b>注意：</b>
     * <ul>
     *   <li>此时Bean已实例化，但依赖尚未注入</li>
     *   <li>不要在此阶段访问Bean的依赖字段</li>
     *   <li>返回的对象会继续进行属性注入</li>
     * </ul>
     *
     * <p><b>使用示例：</b>
     * <pre>{@code
     * @Override
     * public Object afterInstantiation(Object bean, BeanDefinition definition)
     *         throws BeanException {
     *     // 对特定类型的Bean进行包装
     *     if (bean instanceof Traceable) {
     *         return new TraceableWrapper(bean);
     *     }
     *     return bean;
     * }
     * }</pre>
     *
     * @param bean 新创建的Bean实例，永不为null
     * @param definition Bean定义，永不为null
     * @return 要使用的Bean实例（可以是原实例或新实例）
     * @throws BeanException 如果拦截处理失败
     */
    default @NotNull Object afterInstantiation(@NotNull Object bean, @NotNull BeanDefinition definition) throws BeanException {
        return bean;
    }

    /**
     * 在属性注入之后、Aware接口回调之前调用
     *
     * <p><b>作用：</b>
     * <ul>
     *   <li>验证必需的属性是否已注入</li>
     *   <li>执行额外的依赖注入（字段注入、Setter注入）</li>
     *   <li>根据注入的依赖执行逻辑</li>
     * </ul>
     *
     * <p><b>注意：</b>
     * <ul>
     *   <li>此时构造函数参数已注入（如果有）</li>
     *   <li>但Aware接口尚未回调</li>
     *   <li>可以在这里实现自定义的@Autowired注解处理</li>
     * </ul>
     *
     * <p><b>使用示例：</b>
     * <pre>{@code
     * @Override
     * public Object afterPropertyInjection(Object bean, BeanDefinition definition)
     *         throws BeanException {
     *     // 验证必需属性
     *     if (bean instanceof Validatable) {
     *         ((Validatable) bean).validateDependencies();
     *     }
     *     return bean;
     * }
     * }</pre>
     *
     * @param bean Bean实例（已注入属性），永不为null
     * @param definition Bean定义，永不为null
     * @return 要使用的Bean实例（通常返回原实例）
     * @throws BeanException 如果拦截处理失败
     */
    default @NotNull Object afterPropertyInjection(@NotNull Object bean, @NotNull BeanDefinition definition) throws BeanException {
        return bean;
    }

    /**
     * 在Bean初始化之前调用
     *
     * <p><b>作用：</b>
     * <ul>
     *   <li>修改Bean的状态或属性</li>
     *   <li>执行预初始化逻辑</li>
     *   <li>检查Bean是否满足初始化条件</li>
     * </ul>
     *
     * <p><b>调用时机：</b>在以下操作之后：
     * <ul>
     *   <li>实例化</li>
     *   <li>属性注入</li>
     *   <li>Aware接口回调</li>
     * </ul>
     *
     * <p><b>调用时机：</b>在以下操作之前：
     * <ul>
     *   <li>InitializingBean.afterPropertiesSet</li>
     *   <li>自定义init方法</li>
     * </ul>
     *
     * <p><b>使用示例：</b>
     * <pre>{@code
     * @Override
     * public Object beforeInitialization(Object bean, String beanName,
     *                                   BeanDefinition definition) throws BeanException {
     *     // 设置默认值
     *     if (bean instanceof Configurable) {
     *         ((Configurable) bean).setDefaults();
     *     }
     *     return bean;
     * }
     * }</pre>
     *
     * @param bean Bean实例（已注入属性、已回调Aware接口），永不为null
     * @param beanName Bean名称，永不为null或空
     * @param definition Bean定义，永不为null
     * @return 要使用的Bean实例（可以是原实例或包装实例）
     * @throws BeanException 如果拦截处理失败
     */
    default @NotNull Object beforeInitialization(@NotNull Object bean, @NotBlank String beanName, @NotNull BeanDefinition definition) throws BeanException {
        return bean;
    }

    /**
     * 在Bean初始化之后调用
     *
     * <p><b>作用：</b>
     * <ul>
     *   <li><b>创建AOP代理</b>（最常见的用途）</li>
     *   <li>包装Bean添加额外功能</li>
     *   <li>注册Bean到其他系统</li>
     *   <li>收集Bean的元数据</li>
     * </ul>
     *
     * <p><b>调用时机：</b>在以下操作之后：
     * <ul>
     *   <li>实例化</li>
     *   <li>属性注入</li>
     *   <li>Aware接口回调</li>
     *   <li>beforeInitialization拦截</li>
     *   <li>InitializingBean.afterPropertiesSet</li>
     *   <li>自定义init方法</li>
     * </ul>
     *
     * <p><b>这是最后一个修改Bean的机会！</b>
     *
     * <p><b>使用示例：</b>
     * <pre>{@code
     * @Override
     * public Object afterInitialization(Object bean, String beanName,
     *                                  BeanDefinition definition) throws BeanException {
     *     // 为标注了@Transactional的Bean创建代理
     *     if (bean.getClass().isAnnotationPresent(Transactional.class)) {
     *         return createTransactionalProxy(bean);
     *     }
     *     return bean;
     * }
     * }</pre>
     *
     * @param bean Bean实例（已完全初始化），永不为null
     * @param beanName Bean名称，永不为null或空
     * @param definition Bean定义，永不为null
     * @return 要使用的Bean实例（可以是原实例或代理实例）
     * @throws BeanException 如果拦截处理失败
     */
    default @NotNull Object afterInitialization(@NotNull Object bean, @NotBlank String beanName, @NotNull BeanDefinition definition) throws BeanException {
        return bean;
    }

    /**
     * 获取拦截器的顺序
     *
     * <p>顺序值越小，优先级越高（越早执行）。
     *
     * <p><b>默认顺序：</b>0
     *
     * <p><b>建议的顺序范围：</b>
     * <ul>
     *   <li>基础设施拦截器：-1000 ~ -100</li>
     *   <li>框架拦截器：-100 ~ 0</li>
     *   <li>应用拦截器：0 ~ 100</li>
     *   <li>用户拦截器：100 ~ 1000</li>
     * </ul>
     *
     * @return 顺序值
     */
    default int getOrder() {
        return 0;
    }
}