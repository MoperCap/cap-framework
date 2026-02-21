package org.moper.cap.bean.definition;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * @param name  Bean的唯一标识名称
 * @param type Bean的类型
 * @param scope Bean的作用域
 * @param lazy 是否延迟初始化
 * @param primary 是否为主要候选Bean（当存在多个同类型Bean时）
 * @param creation Bean的创建方式（构造函数、工厂方法等）
 * @param initMethodName 初始化方法名
 * @param destroyMethodName 销毁方法名
 * @param dependsOn 依赖的其他Bean名称
 * @param description Bean的描述信息
 */
public record BeanDefinition(
        @NotBlank String name,
        @NotNull Class<?> type,
        @NotNull BeanScope scope,
        boolean lazy,
        boolean primary,
        @NotNull BeanCreation creation,
        @Nullable String initMethodName,
        @Nullable String destroyMethodName,
        @NotNull String[] dependsOn,
        @NotNull String description
) {}
