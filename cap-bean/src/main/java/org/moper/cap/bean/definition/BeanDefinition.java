package org.moper.cap.bean.definition;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * @param name  Bean的唯一标识名称
 * @param type Bean的类型
 * @param scope Bean的作用域
 * @param factoryBeanName 工厂Bean名称（用于实例工厂方法，为null表示不使用）
 * @param factoryMethodName 工厂方法名称（用于静态或实例工厂方法，为null表示使用构造函数）
 * @param constructorArgTypes 构造函数参数类型（或工厂方法参数类型）
 * @param lazy 是否延迟初始化
 * @param primary 是否为主要候选Bean（当存在多个同类型Bean时）
 * @param autowired 是否作为自动装配的候选者
 * @param initMethodName 初始化方法名
 * @param destroyMethodName 销毁方法名
 * @param dependsOn 依赖的其他Bean名称
 * @param description Bean的描述信息
 */
public record BeanDefinition(
        @NotBlank String name,
        @NotNull Class<?> type,
        @NotNull BeanScope scope,
        @Nullable String factoryBeanName,
        @Nullable String factoryMethodName,
        @NotNull Class<?>[] constructorArgTypes,
        boolean lazy,
        boolean primary,
        boolean autowired,
        @Nullable String initMethodName,
        @Nullable String destroyMethodName,
        @NotNull String[] dependsOn,
        @NotNull String description
) {

    /**
     * 判断是否通过构造函数实例化
     *
     * @return 如果factoryMethodName为null，则使用构造函数
     */
    public boolean isConstructorInstantiation() {
        return factoryMethodName == null;
    }

    /**
     * 判断是否通过静态工厂方法实例化
     *
     * @return 如果factoryMethodName不为null且factoryBeanName为null，则为静态工厂方法
     */
    public boolean isStaticFactoryMethod() {
        return factoryMethodName != null && factoryBeanName == null;
    }

    /**
     * 判断是否通过实例工厂方法实例化
     *
     * @return 如果factoryMethodName和factoryBeanName都不为null，则为实例工厂方法
     */
    public boolean isInstanceFactoryMethod() {
        return factoryMethodName != null && factoryBeanName != null;
    }

}
