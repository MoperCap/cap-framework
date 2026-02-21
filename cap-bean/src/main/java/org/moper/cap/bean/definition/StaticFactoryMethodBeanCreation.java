package org.moper.cap.bean.definition;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 静态工厂方法实例化策略
 * 通过调用类的静态方法创建Bean实例
 * 例如：MyClass.createInstance()
 *
 * @param factoryClassName 工厂类的全限定名（静态方法所在的类）
 * @param factoryMethodName 静态工厂方法名称
 * @param parameterTypes 工厂方法参数类型数组
 */
public record StaticFactoryMethodBeanCreation(
        @NotBlank String factoryClassName,
        @NotBlank String factoryMethodName,
        @NotNull Class<?>[] parameterTypes
) implements BeanCreation {

    /**
     * 创建无参静态工厂方法的实例化策略
     *
     * @param factoryClassName 工厂类名
     * @param factoryMethodName 静态工厂方法名称
     * @return 静态工厂方法实例化策略
     */
    public static StaticFactoryMethodBeanCreation noArgs(
            @NotBlank String factoryClassName,
            @NotBlank String factoryMethodName) {
        return new StaticFactoryMethodBeanCreation(factoryClassName, factoryMethodName, new Class<?>[0]);
    }

    /**
     * 创建无参静态工厂方法的实例化策略（通过Class对象）
     *
     * @param factoryClass 工厂类
     * @param factoryMethodName 静态工厂方法名称
     * @return 静态工厂方法实例化策略
     */
    public static StaticFactoryMethodBeanCreation noArgs(
            @NotNull Class<?> factoryClass,
            @NotBlank String factoryMethodName) {
        return new StaticFactoryMethodBeanCreation(factoryClass.getName(), factoryMethodName, new Class<?>[0]);
    }

    /**
     * 创建有参静态工厂方法的实例化策略
     *
     * @param factoryClassName 工厂类名
     * @param factoryMethodName 静态工厂方法名称
     * @param parameterTypes 工厂方法参数类型
     * @return 静态工厂方法实例化策略
     */
    public static StaticFactoryMethodBeanCreation withArgs(
            @NotBlank String factoryClassName,
            @NotBlank String factoryMethodName,
            @NotNull Class<?>... parameterTypes) {
        return new StaticFactoryMethodBeanCreation(factoryClassName, factoryMethodName, parameterTypes);
    }

    /**
     * 创建有参静态工厂方法的实例化策略（通过Class对象）
     *
     * @param factoryClass 工厂类
     * @param factoryMethodName 静态工厂方法名称
     * @param parameterTypes 工厂方法参数类型
     * @return 静态工厂方法实例化策略
     */
    public static StaticFactoryMethodBeanCreation withArgs(
            @NotNull Class<?> factoryClass,
            @NotBlank String factoryMethodName,
            @NotNull Class<?>... parameterTypes) {
        return new StaticFactoryMethodBeanCreation(factoryClass.getName(), factoryMethodName, parameterTypes);
    }

    /**
     * 判断是否为无参工厂方法
     *
     * @return 是否为无参工厂方法
     */
    public boolean isNoArgsFactoryMethod() {
        return parameterTypes.length == 0;
    }

    @Override
    public BeanCreationType type() {
        return BeanCreationType.STATIC_FACTORY_METHOD;
    }
}
