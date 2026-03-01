package org.moper.cap.bean.definition.instantiation;

/**
 * 基于构造函数的实例化策略 </br>
 *
 * @param beanClass 要实例化的类，不能为 null
 * @param argTypes 构造函数参数类型，按顺序排列，无参时为空数组，不能为 null
 */
public record ConstructorInstantiation(
        Class<?> beanClass,
        Class<?>[] argTypes
) implements InstantiationPolicy {

    public ConstructorInstantiation{
        if(beanClass == null) {
            throw new IllegalArgumentException("ConstructorInstantiation beanClass cannot be null");
        }

        if (argTypes == null) {
            throw new IllegalArgumentException("ConstructorInstantiation argTypes cannot be null");
        }
    }

    /**
     * 无参构造函数实例化
     *
     * @param beanClass 要实例化的类，不能为 null
     * @return 构造函数实例化策略实例
     */
    public static ConstructorInstantiation of(Class<?> beanClass) {
        return new ConstructorInstantiation(beanClass, new Class<?>[0]);
    }


    /**
     * 指定参数类型的构造函数实例化
     *
     * @param beanClass 要实例化的类，不能为 null
     * @param argTypes 构造函数参数类型，按顺序排列，不能为 null
     * @return 构造函数实例化策略实例
     */
    public static ConstructorInstantiation of(Class<?> beanClass, Class<?>... argTypes) {
        return new ConstructorInstantiation(beanClass, argTypes);
    }
}
