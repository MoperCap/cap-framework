package org.moper.cap.bean.registry;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.moper.cap.bean.definition.BeanDefinition;

/**
 * Bean定义注册表接口
 * 提供Bean定义的注册、移除和查询能力
 */
public interface BeanDefinitionRegistry {
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
     * @throws BeanException 若移除失败
     */
    void removeBeanDefinition(@NotBlank String beanName) throws BeanException;

    /**
     * 获取Bean定义
     *
     * @param beanName Bean名称
     * @return Bean定义
     * @throws BeanException 若Bean定义不存在
     */
    @NotNull BeanDefinition getBeanDefinition(@NotBlank String beanName) throws BeanException;

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
    @NotNull String[] getBeanDefinitionNames();

    /**
     * 获取Bean定义的数量
     *
     * @return Bean定义数量
     */
    int getBeanDefinitionCount();

    /**
     * 检查Bean名称是否已被使用（包括别名）
     *
     * @param beanName Bean名称
     * @return 是否已被使用
     */
    boolean isBeanNameInUse(@NotBlank String beanName);
}
