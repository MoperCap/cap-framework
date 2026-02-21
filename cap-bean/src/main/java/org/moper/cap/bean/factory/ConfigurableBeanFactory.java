package org.moper.cap.bean.factory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.exception.BeanException;
import org.moper.cap.bean.processor.BeanPostProcessor;

/**
 * 可配置的Bean工厂接口
 * 提供Bean定义的注册和管理能力
 */
public interface ConfigurableBeanFactory extends BeanFactory {

    /**
     * 注册Bean定义
     *
     * @param beanDefinition Bean定义
     * @throws BeanException 若注册失败（如Bean名称冲突）
     */
    void registerBeanDefinition(@NotNull BeanDefinition beanDefinition) throws BeanException;

    /**
     * 移除Bean定义
     *
     * @param beanName Bean名称
     * @throws BeanException 若移除失败或配置已冻结
     */
    void removeBeanDefinition(@NotBlank String beanName) throws BeanException;

    /**
     * 获���Bean定义
     *
     * @param beanName Bean名称
     * @return Bean定义
     * @throws BeanException 若Bean定义不存在
     */
    @NotNull
    BeanDefinition getBeanDefinition(@NotBlank String beanName) throws BeanException;

    /**
     * 检查是否包含指定名称的Bean定义
     *
     * @param beanName Bean名称
     * @return 是否包含
     */
    boolean containsBeanDefinition(@NotBlank String beanName);

    /**
     * 获取所有Bean定义的名称
     *
     * @return Bean定义名称数组
     */
    @NotNull
    String[] getBeanDefinitionNames();

    /**
     * 获取指定类型的所有Bean定义名称
     *
     * @param type 类型
     * @return Bean定义名称数组
     */
    @NotNull
    String[] getBeanNamesForType(@NotNull Class<?> type);

    /**
     * 注册Bean别名
     *
     * @param beanName Bean名称
     * @param alias 别名
     * @throws BeanException 若注册失败（如别名冲突）或配置已冻结
     */
    void registerAlias(@NotBlank String beanName, @NotBlank String alias) throws BeanException;

    /**
     * 注册单例Bean实例
     * 用于注册外部创建的单例对象
     *
     * @param beanName Bean名称
     * @param singletonObject 单例对象
     * @throws BeanException 若注册失败或配置已冻结
     */
    void registerSingleton(@NotBlank String beanName, @NotNull Object singletonObject) throws BeanException;

    /**
     * 添加Bean后置处理器
     *
     * @param beanPostProcessor Bean后置处理器
     * @throws BeanException 若配置已冻结
     */
    void addBeanPostProcessor(@NotNull BeanPostProcessor beanPostProcessor) throws BeanException;

    /**
     * 获取Bean定义的数量
     *
     * @return Bean定义数量
     */
    int getBeanDefinitionCount();

    /**
     * 预实例化所有非懒加载的单例Bean
     * 通常在配置完成后调用
     *
     * @throws BeanException 若实例化失败
     */
    void preInstantiateSingletons() throws BeanException;

    /**
     * 冻结配置，使工厂进入只读状态
     * 调用后不允许再修改Bean定义和配置
     */
    void freezeConfiguration();

    /**
     * 检查配置是否已冻结
     *
     * @return 是否已冻结
     */
    boolean isConfigurationFrozen();

    /**
     * 销毁所有单例Bean
     *
     * @throws BeanException 若销毁失败
     */
    void destroySingletons() throws BeanException;

}
