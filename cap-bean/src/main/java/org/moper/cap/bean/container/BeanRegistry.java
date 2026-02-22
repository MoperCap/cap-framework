package org.moper.cap.bean.container;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.exception.BeanException;

/**
 * Bean注册器接口
 *
 * <p><b>核心语义：提供注册和修改BeanDefinition的能力</b>
 *
 * <p><b>心智模型：</b>把它理解成"容器的结构修改器"
 */
public interface BeanRegistry {

    /**
     * 注册Bean定义
     *
     * @param beanDefinition Bean定义，不能为null
     * @throws BeanException 如果注册失败（如Bean名称冲突或配置已冻结）
     */
    void registerBeanDefinition(@NotNull BeanDefinition beanDefinition) throws BeanException;

    /**
     * 移除Bean定义
     *
     * @param beanName Bean名称，不能为空
     * @throws BeanException 如果移除失败、Bean不存在或配置已冻结
     */
    void removeBeanDefinition(@NotBlank String beanName) throws BeanException;

    /**
     * 注册Bean别名
     *
     * @param beanName Bean名称，不能为空
     * @param alias 别名，不能为空
     * @throws BeanException 如果注册失败（如别名冲突、Bean不存在或配置已冻结）
     */
    void registerAlias(@NotBlank String beanName, @NotBlank String alias) throws BeanException;

    /**
     * 注册单例Bean实例
     *
     * @param beanName Bean名称，不能为空
     * @param singletonObject 单例对象，不能为null
     * @throws BeanException 如果注册失败（如名称冲突或配置已冻结）
     */
    void registerSingleton(@NotBlank String beanName, @NotNull Object singletonObject) throws BeanException;

    /**
     * 检查Bean名称是否已被使用
     *
     * @param beanName Bean名称
     * @return 如果已被使用返回true，否则返回false
     */
    boolean isBeanNameInUse(@NotBlank String beanName);
}