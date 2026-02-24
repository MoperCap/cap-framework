package org.moper.cap.bean.container;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.moper.cap.bean.exception.BeanCreationException;
import org.moper.cap.bean.exception.BeanNotOfRequiredTypeException;
import org.moper.cap.bean.exception.NoSuchBeanDefinitionException;
import org.moper.cap.bean.exception.NoUniqueBeanDefinitionException;

/**
 * Bean 提供者接口。
 *
 * <p><b>核心语义：IoC 容器对外暴露的最小只读能力，仅负责获取 Bean 实例</b>
 */
public interface BeanProvider {

    /**
     * 按名称获取 Bean 实例。
     *
     * @param beanName Bean 名称（或别名），不能为空
     * @return Bean 实例，永不为 null
     * @throws NoSuchBeanDefinitionException 如果 Bean 不存在
     * @throws BeanCreationException         如果创建失败
     */
    Object getBean(String beanName) throws NoSuchBeanDefinitionException, BeanCreationException;

    /**
     * 按名称获取指定类型的 Bean 实例，含类型校验。
     *
     * @param beanName     Bean 名称（或别名），不能为空
     * @param requiredType 期望的类型，不能为 null
     * @param <T>          类型参数
     * @return Bean 实例，永不为 null
     * @throws NoSuchBeanDefinitionException   如果 Bean 不存在
     * @throws BeanCreationException           如果创建失败
     * @throws BeanNotOfRequiredTypeException 如果类型不匹配
     */
    <T> T getBean(String beanName, Class<T> requiredType)throws NoSuchBeanDefinitionException, BeanCreationException, BeanNotOfRequiredTypeException;

    /**
     * 按类型获取唯一匹配的 Bean 实例。
     *
     * <p>若存在多个匹配 Bean，有且仅有一个标记为 {@code primary} 则返回该 Bean，
     * 否则抛出 {@link NoUniqueBeanDefinitionException}。
     *
     * @param requiredType 期望的类型，不能为 null
     * @param <T>          类型参数
     * @return Bean 实例，永不为 null
     * @throws NoSuchBeanDefinitionException   如果 Bean 不存在
     * @throws BeanCreationException           如果创建失败
     * @throws NoUniqueBeanDefinitionException 如果存在多个匹配 Bean 且没有唯一的 primary
     */
    <T> T getBean(Class<T> requiredType) throws NoSuchBeanDefinitionException, BeanCreationException, NoUniqueBeanDefinitionException;
}