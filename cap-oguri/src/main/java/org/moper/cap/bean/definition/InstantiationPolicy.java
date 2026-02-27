package org.moper.cap.bean.definition;

import org.moper.cap.bean.exception.BeanDefinitionStoreException;

/**
 * Bean 的实例化策略，描述容器应当以何种方式创建 Bean 实例。
 *
 * <p>三种策略互斥，通过字段组合区分：
 * <table border="1">
 *   <tr><th>策略</th><th>factoryBeanName</th><th>factoryMethodName</th></tr>
 *   <tr><td>构造函数</td><td>null</td><td>null</td></tr>
 *   <tr><td>静态工厂</td><td>null</td><td>非 null</td></tr>
 *   <tr><td>实例工厂</td><td>非 null</td><td>非 null</td></tr>
 * </table>
 *
 * <p>推荐通过静态工厂方法创建：
 * <ul>
 *   <li>{@link #constructor(Class[])} —— 构造函数策略</li>
 *   <li>{@link #staticFactory(String, Class[])} —— 静态工厂策略</li>
 *   <li>{@link #instanceFactory(String, String, Class[])} —— 实例工厂策略</li>
 * </ul>
 *
 * @param factoryBeanName   实例工厂的 Bean 名称；仅实例工厂策略时非 null，其余情况为 null
 * @param factoryMethodName 工厂方法名称；静态工厂和实例工厂策略时非 null，构造函数策略为 null
 * @param argTypes          构造函数或工厂方法的参数类型，按参数顺序排列，不能为 null，
 *                          无参时为空数组
 */
public record InstantiationPolicy(
        String   factoryBeanName,
        String   factoryMethodName,
        Class<?>[] argTypes
) {

    /**
     * 构造函数实例化策略（无参构造）。
     *
     * @return 新的 InstantiationPolicy 实例
     */
    public static InstantiationPolicy constructor() {
        return new InstantiationPolicy(null, null, new Class<?>[0]);
    }

    /**
     * 构造函数实例化策略（有参构造）。
     *
     * @param argTypes 构造函数参数类型，按顺序排列，不能为 null
     * @return 新的 InstantiationPolicy 实例
     */
    public static InstantiationPolicy constructor(Class<?>... argTypes) {
        return new InstantiationPolicy(null, null, argTypes);
    }

    /**
     * 静态工厂方法实例化策略。
     *
     * @param methodName 静态工厂方法名，不能为空，必须在 Bean 的 {@code type} 类上声明
     * @param argTypes   方法参数类型，按顺序排列，不能为 null
     * @return 新的 InstantiationPolicy 实例
     * @throws BeanDefinitionStoreException 如果 methodName 为空
     */
    public static InstantiationPolicy staticFactory(
            String methodName,
            Class<?>... argTypes) {
        if (methodName == null || methodName.isBlank()) {
            throw new BeanDefinitionStoreException(
                    "Static factory method name must not be blank");
        }
        return new InstantiationPolicy(null, methodName, argTypes);
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
    public static InstantiationPolicy instanceFactory(
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
        return new InstantiationPolicy(factoryBeanName, methodName, argTypes);
    }

    /** 是否为构造函数实例化策略 */
    public boolean isConstructor() {
        return factoryMethodName == null;
    }

    /** 是否为静态工厂方法实例化策略 */
    public boolean isStaticFactory() {
        return factoryMethodName != null && factoryBeanName == null;
    }

    /** 是否为实例工厂方法实例化策略 */
    public boolean isInstanceFactory() {
        return factoryMethodName != null && factoryBeanName != null;
    }
}