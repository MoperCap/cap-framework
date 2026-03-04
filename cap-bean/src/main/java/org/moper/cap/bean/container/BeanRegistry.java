package org.moper.cap.bean.container;

import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.exception.BeanDefinitionStoreException;
import org.moper.cap.bean.exception.NoSuchBeanDefinitionException;

/**
 * Bean 注册器接口。
 *
 * <p><b>核心语义：对容器结构的写入能力</b>
 *
 * <p>此接口只表达"能做什么"（写入 BeanDefinition、注册别名、注册外部单例），
 * 不涉及任何生命周期编排（冻结、预实例化、销毁等）。
 * 生命周期编排由持有此接口的上层组件（{@code BootstrapContext} / {@code ApplicationContext}）负责。
 *
 * <p><b>心智模型：</b>把它理解成"容器的结构修改器"，类比数据库的 DDL 操作。
 */
public interface BeanRegistry {

    /**
     * 注册一个 BeanDefinition，以 {@link BeanDefinition#name()} 作为注册 key。
     *
     * <p>若同名 BeanDefinition 已存在则抛出异常。
     * 如需覆盖，应先调用 {@link #removeBeanDefinition(String)} 再重新注册，
     * 或通过 {@link BeanDefinition#withLazy(boolean)} 等 wither 方法创建新实例后替换。
     *
     * @param beanDefinition 不能为 null
     * @throws BeanDefinitionStoreException 如果同名 BeanDefinition 已存在
     */
    void registerBeanDefinition(BeanDefinition beanDefinition) throws BeanDefinitionStoreException;

    /**
     * 移除指定名称的 BeanDefinition 及其对应的单例缓存（如果存在）。
     *
     * @param beanName 不能为空
     * @throws NoSuchBeanDefinitionException 如果不存在
     */
    void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

    /**
     * 为指定 Bean 注册别名。
     *
     * <p>别名可以用于 {@link BeanProvider#getBean(String)} 等所有按名称查找的场景。
     *
     * @param beanName 目标 Bean 的名称，不能为空
     * @param alias    别名，不能为空，不能与已有名称或别名重复
     * @throws NoSuchBeanDefinitionException 如果 Bean 不存在
     * @throws BeanDefinitionStoreException 如果目别名已被占用
     */
    void registerAlias(String beanName, String alias) throws NoSuchBeanDefinitionException, BeanDefinitionStoreException;

    /**
     * 移除指定别名。
     *
     * @param alias 不能为空
     * @throws BeanDefinitionStoreException 如果别名不存在
     */
    void removeAlias(String alias) throws BeanDefinitionStoreException;

    /**
     * 直接注册一个已存在的单例对象，绕过完整创建流程，不触发任何生命周期回调。
     *
     * <p>适用于将框架外部已创建好的对象（如系统资源、第三方库对象）纳入容器管理。
     * 通过此方法注册的单例在 {@link BeanInspector#containsBean(String)} 时可被感知，
     * 但不存在对应的 BeanDefinition，{@link BeanInspector#containsBeanDefinition(String)}
     * 返回 {@code false}。
     *
     * @param beanName        Bean 名称，不能为空
     * @param singletonObject 单例对象，不能为 null
     * @throws BeanDefinitionStoreException 如果名称已被占用
     */
    void registerSingleton(String beanName, Object singletonObject) throws BeanDefinitionStoreException;

    /**
     * 判断指定名称是否已被使用（含已注册的 BeanDefinition、单例实例或别名）。
     *
     * <p>在注册前可调用此方法进行预检，避免触发异常。
     *
     * @param beanName 不能为空
     * @return 已被使用则返回 {@code true}
     */
    boolean isBeanNameInUse(String beanName);
}