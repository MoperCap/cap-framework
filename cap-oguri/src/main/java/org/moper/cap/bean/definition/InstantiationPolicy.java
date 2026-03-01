package org.moper.cap.bean.definition;

import org.moper.cap.bean.exception.BeanDefinitionStoreException;

/**
 * Bean 的实例化策略，描述容器应当以何种方式创建 Bean 实例。
 *
 * <p>通过 sealed interface + record 机制将三种策略封装为独立类型：
 * <ul>
 *   <li>{@link ConstructorInstantiation} —— 基于构造函数初始化策略</li>
 *   <li>{@link StaticFactoryInstantiation} —— 基于静态工厂函数初始化策略</li>
 *   <li>{@link InstanceFactoryInstantiation} —— 基于实例化工厂函数初始化策略</li>
 * </ul>
 *
 * <p>推荐通过静态工厂方法创建：
 * <ul>
 *   <li>{@link #constructor(Class[])} —— 构造函数策略</li>
 *   <li>{@link #staticFactory(String, String, Class[])} —— 静态工厂策略</li>
 *   <li>{@link #instanceFactory(String, String, Class[])} —— 实例工厂策略</li>
 * </ul>
 */
public sealed interface InstantiationPolicy
        permits InstantiationPolicy.ConstructorInstantiation,
                InstantiationPolicy.StaticFactoryInstantiation,
                InstantiationPolicy.InstanceFactoryInstantiation {

    /**
     * 构造函数或工厂方法的参数类型，按参数顺序排列，无参时为空数组。
     */
    Class<?>[] argTypes();

    // ==================== 嵌套 record 实现 ====================

    /**
     * 基于构造函数的实例化策略。
     *
     * @param argTypes 构造函数参数类型，按顺序排列，无参时为空数组
     */
    record ConstructorInstantiation(Class<?>[] argTypes)
            implements InstantiationPolicy {}

    /**
     * 基于静态工厂方法的实例化策略。
     *
     * @param factoryBeanName   静态工厂方法所在类对应的 Bean 名称，不能为空
     * @param factoryMethodName 静态工厂方法名称，不能为空
     * @param argTypes          方法参数类型，按顺序排列，无参时为空数组
     */
    record StaticFactoryInstantiation(
            String factoryBeanName,
            String factoryMethodName,
            Class<?>[] argTypes)
            implements InstantiationPolicy {}

    /**
     * 基于实例工厂方法的实例化策略。
     *
     * @param factoryBeanName   工厂 Bean 的注册名称，不能为空
     * @param factoryMethodName 工厂方法名称，不能为空
     * @param argTypes          方法参数类型，按顺序排列，无参时为空数组
     */
    record InstanceFactoryInstantiation(
            String factoryBeanName,
            String factoryMethodName,
            Class<?>[] argTypes)
            implements InstantiationPolicy {}

    // ==================== 静态工厂方法 ====================

    /**
     * 构造函数实例化策略（无参构造）。
     *
     * @return 新的 InstantiationPolicy 实例
     */
    static InstantiationPolicy constructor() {
        return new ConstructorInstantiation(new Class<?>[0]);
    }

    /**
     * 构造函数实例化策略（有参构造）。
     *
     * @param argTypes 构造函数参数类型，按顺序排列，不能为 null
     * @return 新的 InstantiationPolicy 实例
     */
    static InstantiationPolicy constructor(Class<?>... argTypes) {
        return new ConstructorInstantiation(argTypes);
    }

    /**
     * 静态工厂方法实例化策略。
     *
     * @param factoryBeanName 静态工厂方法所在类对应的 Bean 名称，不能为空
     * @param methodName      静态工厂方法名，不能为空
     * @param argTypes        方法参数类型，按顺序排列，不能为 null
     * @return 新的 InstantiationPolicy 实例
     * @throws BeanDefinitionStoreException 如果 factoryBeanName 或 methodName 为空
     */
    static InstantiationPolicy staticFactory(
            String factoryBeanName,
            String methodName,
            Class<?>... argTypes) {
        if (factoryBeanName == null || factoryBeanName.isBlank()) {
            throw new BeanDefinitionStoreException(
                    "Static factory bean name must not be blank");
        }
        if (methodName == null || methodName.isBlank()) {
            throw new BeanDefinitionStoreException(
                    "Static factory method name must not be blank");
        }
        return new StaticFactoryInstantiation(factoryBeanName, methodName, argTypes);
    }

    /**
     * 实例工厂方法实例化策略。
     *
     * @param factoryBeanName 工厂 Bean 的注册名称，不能为空
     * @param methodName      工厂方法名，不能为空
     * @param argTypes        方法参数类型，按顺序排列，不能为 null
     * @return 新的 InstantiationPolicy 实例
     * @throws BeanDefinitionStoreException 如果 factoryBeanName 或 methodName 为空
     */
    static InstantiationPolicy instanceFactory(
            String factoryBeanName,
            String methodName,
            Class<?>... argTypes) {
        if (factoryBeanName == null || factoryBeanName.isBlank()) {
            throw new BeanDefinitionStoreException(
                    "Factory bean name must not be blank");
        }
        if (methodName == null || methodName.isBlank()) {
            throw new BeanDefinitionStoreException(
                    "Instance factory method name must not be blank");
        }
        return new InstanceFactoryInstantiation(factoryBeanName, methodName, argTypes);
    }

    // ==================== 类型判断 ====================

    /** 是否为构造函数实例化策略 */
    default boolean isConstructor() {
        return this instanceof ConstructorInstantiation;
    }

    /** 是否为静态工厂方法实例化策略 */
    default boolean isStaticFactory() {
        return this instanceof StaticFactoryInstantiation;
    }

    /** 是否为实例工厂方法实例化策略 */
    default boolean isInstanceFactory() {
        return this instanceof InstanceFactoryInstantiation;
    }
}
