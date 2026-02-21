package org.moper.cap.bean.definition;


import jakarta.validation.constraints.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Bean依赖描述符
 * 描述Bean的依赖注入信息
 *
 * @param dependencyType 依赖的类型
 * @param dependencyName 依赖的名称（可选，用于按名称注入）
 * @param required 是否必需
 * @param fieldName 字段名（字段注入时使用）
 * @param parameterIndex 参数索引（构造器注入或方法注入时使用）
 */
public record DependencyDescriptor(
        @NotNull Class<?> dependencyType,
        @Nullable String dependencyName,
        boolean required,
        @Nullable String fieldName,
        @Nullable Integer parameterIndex
) {}
