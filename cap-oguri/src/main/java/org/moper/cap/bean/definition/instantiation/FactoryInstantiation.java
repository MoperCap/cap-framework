package org.moper.cap.bean.definition.instantiation;

/**
 * 基于工厂方法的实例化策略
 *
 * @param factoryBeanName 工厂方法所在类对应的 Bean 名称，不能为null
 * @param factoryMethodName 工厂方法名称，不能为null
 * @param argTypes 工厂方法参数类型，按顺序排列，无参时为空数组，不能为null
 */
public record FactoryInstantiation(
        String factoryBeanName,
        String factoryMethodName,
        Class<?>[] argTypes
) implements InstantiationPolicy {

    public FactoryInstantiation {
        if(factoryBeanName == null || factoryBeanName.isBlank()) {
            throw new IllegalArgumentException("FactoryInstantiation factoryBeanName cannot be null or blank");
        }

        if (factoryMethodName == null || factoryMethodName.isBlank()) {
            throw new IllegalArgumentException("FactoryInstantiation factoryMethodName cannot be null or blank");
        }

        if (argTypes == null) {
            throw new IllegalArgumentException("FactoryInstantiation argTypes cannot be null");
        }

    }

    /**
     * 工厂方法实例化的快捷创建方法，适用于无参工厂方法。
     *
     * @param factoryBeanName 工厂方法所在类对应的 Bean 名称，不能为null
     * @param factoryMethodName 工厂方法名称，不能为null
     * @return 一个新的 FactoryInstantiation 实例，使用空参数类型数组
     */
    public static FactoryInstantiation of(String factoryBeanName, String factoryMethodName){
        return new FactoryInstantiation(factoryBeanName, factoryMethodName, new Class<?>[0]);
    }

    /**
     * 工厂方法实例化的快捷创建方法，适用于有参工厂方法。
     *
     * @param factoryBeanName 工厂方法所在类对应的 Bean 名称，不能为null
     * @param factoryMethodName 工厂方法名称，不能为null
     * @param argTypes 工厂方法参数类型，按顺序排列，不能为null
     * @return 一个新的 FactoryInstantiation 实例，使用提供的参数类型数组
     */
    public static FactoryInstantiation of(String factoryBeanName, String factoryMethodName, Class<?>... argTypes){
        return new FactoryInstantiation(factoryBeanName, factoryMethodName, argTypes);
    }

}
