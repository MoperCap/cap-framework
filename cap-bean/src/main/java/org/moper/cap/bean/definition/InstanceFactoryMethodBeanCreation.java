package org.moper.cap.bean.definition;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 实例工厂方法实例化策略
 * 通过调用工厂Bean实例的方法创建Bean实例
 * 例如：factoryBean.createInstance()
 *
 * @param factoryBeanName 工厂Bean的名称（必须是容器中已存在的Bean）
 * @param factoryMethodName 工厂方法名称
 * @param parameterTypes 工厂方法参数类型数组
 */
public record InstanceFactoryMethodBeanCreation(
        @Nullable String factoryBeanName,
        @NotBlank String factoryMethodName,
        @NotNull Class<?>[] parameterTypes
) implements BeanCreation {

    /**
     * 创建无参实例工厂方法的实例化策略
     *
     * @param factoryBeanName 工厂Bean名称
     * @param factoryMethodName 工厂方法名称
     * @return 实例工厂方法实例化策略
     */
    public static InstanceFactoryMethodBeanCreation noArgs(
            @NotBlank String factoryBeanName,
            @NotBlank String factoryMethodName) {
        return new InstanceFactoryMethodBeanCreation(factoryBeanName, factoryMethodName, new Class<?>[0]);
    }

    /**
     * 创建有参实例工厂方法的实例化策略
     *
     * @param factoryBeanName 工厂Bean名称
     * @param factoryMethodName 工厂方法名称
     * @param parameterTypes 工厂方法参数类型
     * @return 实例工厂方法实例化策略
     */
    public static InstanceFactoryMethodBeanCreation withArgs(
            @NotBlank String factoryBeanName,
            @NotBlank String factoryMethodName,
            @NotNull Class<?>... parameterTypes) {
        return new InstanceFactoryMethodBeanCreation(factoryBeanName, factoryMethodName, parameterTypes);
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
        return BeanCreationType.INSTANCE_FACTORY_METHOD;
    }
}
