package org.moper.cap.bean.definition;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * 构造函数实例化策略
 * 通过调用目标类的构造函数创建Bean实例
 *
 * @param parameterTypes 构造函数参数类型数组，空数组表示无参构造函数
 */
public record ConstructorBeanCreation(
        @NotNull Class<?>[] parameterTypes
) implements BeanCreation {
    /**
     * 创建无参构造函数的实例化策略
     *
     * @return 无参构造函数实例化策略
     */
    public static ConstructorBeanCreation noArgs() {
        return new ConstructorBeanCreation(new Class<?>[0]);
    }

    /**
     * 创建有参构造函数的实例化策略
     *
     * @param parameterTypes 构造函数参数类型
     * @return 有参构造函数实例化策略
     */
    public static ConstructorBeanCreation withArgs(@NotNull Class<?>... parameterTypes) {
        return new ConstructorBeanCreation(parameterTypes);
    }

    /**
     * 判断是否为无参构造函数
     *
     * @return 是否为无参构造函数
     */
    public boolean isNoArgsConstructor() {
        return parameterTypes.length == 0;
    }

    @Override
    public BeanCreationType type() {
        return BeanCreationType.CONSTRUCTOR;
    }
}
