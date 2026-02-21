package org.moper.cap.bean.exception;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/**
 * 依赖未满足异常
 * 当Bean的某个依赖无法被解析时抛出
 */
@Getter
public class UnsatisfiedDependencyException extends BeanDependencyInjectionException {
    
    private final Class<?> dependencyType;

    public UnsatisfiedDependencyException(@NotBlank String beanName,
                                         @Nullable String propertyName,
                                         @NotNull Class<?> dependencyType) {
        super(beanName, propertyName, 
              "No qualifying bean of type '" + dependencyType.getName() + "' available");
        this.dependencyType = dependencyType;
    }

    public UnsatisfiedDependencyException(@NotBlank String beanName,
                                         @Nullable String propertyName,
                                         @NotNull Class<?> dependencyType,
                                         String message) {
        super(beanName, propertyName, 
              "No qualifying bean of type '" + dependencyType.getName() + "' available: " + message);
        this.dependencyType = dependencyType;
    }

}