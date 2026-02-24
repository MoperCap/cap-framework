package org.moper.cap.bean.container;

import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.exception.BeanCreationException;
import org.moper.cap.bean.exception.NoSuchBeanDefinitionException;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Bean 检查器接口。
 *
 * <p><b>核心语义：对容器结构的只读查询能力</b>
 *
 * <p>按名称/注解查询只返回名称数组，不触发实例化；
 * {@link #getBeansOfType} 和 {@link #getBeansWithAnnotation} 会触发实例化。
 */
public interface BeanInspector {

    /**
     * 判断容器中是否包含指定名称的 Bean（含别名和已注册单例）
     */
    boolean containsBean(String beanName);

    /**
     * 判断是否存在指定名称的 BeanDefinition（不含纯单例注册）
     */
    boolean containsBeanDefinition(String beanName);

    /**
     * 获取指定名称的 BeanDefinition。
     *
     * @throws NoSuchBeanDefinitionException 如果不存在
     */
    BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

    /**
     * 获取所有 BeanDefinition 的名称，无则返回空数组
     */
    String[] getBeanDefinitionNames();

    /**
     * 获取 BeanDefinition 的总数
     */
    int getBeanDefinitionCount();

    /**
     * 获取所有匹配指定类型的 Bean 名称（含子类型），不触发实例化，无则返回空数组
     */
    String[] getBeanNamesForType(Class<?> type);

    /**
     * 获取所有标注了指定注解的 Bean 名称，不触发实例化，无则返回空数组
     */
    String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType);

    /**
     * 获取所有匹配指定类型的 Bean 实例，会触发实例化。
     *
     * @return 名称 → 实例 的映射，无则返回空 Map
     * @throws BeanCreationException 如果任意 Bean 实例化失败
     */
    <T> Map<String, T> getBeansOfType(Class<T> type) throws BeanCreationException;

    /**
     * 获取所有标注了指定注解的 Bean 实例，会触发实例化。
     *
     * @return 名称 → 实例 的映射，无则返回空 Map
     * @throws BeanCreationException 如果任意 Bean 实例化失败
     */
    Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeanCreationException;

    /**
     * 判断指定 Bean 是否为单例作用域。
     *
     * @throws NoSuchBeanDefinitionException 如果 Bean 不存在
     */
    boolean isSingleton(String beanName) throws NoSuchBeanDefinitionException;

    /**
     * 判断指定 Bean 是否为原型作用域。
     *
     * @throws NoSuchBeanDefinitionException 如果 Bean 不存在
     */
    boolean isPrototype(String beanName) throws NoSuchBeanDefinitionException;

    /**
     * 判断指定 Bean 是否可赋值为目标类型。
     *
     * @throws NoSuchBeanDefinitionException 如果 Bean 不存在
     */
    boolean isTypeMatch(String beanName, Class<?> targetType) throws NoSuchBeanDefinitionException;

    /**
     *  获取指定 Bean 的实际类型
     *
     * @throws NoSuchBeanDefinitionException 如果 Bean 不存在
     */
    Class<?> getType(String beanName) throws NoSuchBeanDefinitionException;

    /**
     * 获取指定 Bean 的所有别名，无别名则返回空数组。
     *
     * <p>入参可以是规范名，也可以是别名：
     * <ul>
     *   <li>传入规范名 {@code "dataSource"}：返回所有指向该 Bean 的别名</li>
     *   <li>传入别名 {@code "dsAlias"}：等价于传入其对应的规范名，返回包含自身在内的所有别名</li>
     * </ul>
     *
     * @param beanName 规范名或别名，不能为空
     * @return 所有别名数组（含入参自身，若入参为别名），无别名则返回空数组
     * @throws NoSuchBeanDefinitionException 如果 Bean 不存在
     */
    String[] getAliases(String beanName) throws NoSuchBeanDefinitionException;

    /**
     * 在指定 Bean 上查找注解（含继承层级），不存在则返回 null。
     *
     * @throws NoSuchBeanDefinitionException 如果 Bean 不存在
     */
    <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType) throws NoSuchBeanDefinitionException;
}