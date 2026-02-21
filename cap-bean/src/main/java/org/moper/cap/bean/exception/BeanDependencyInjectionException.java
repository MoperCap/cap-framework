package org.moper.cap.bean.exception;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/**
 * Bean依赖注入异常
 * 在进行依赖注入时发生的异常
 * 这是一个功能性异常，不属于生命周期范畴
 */
@Getter
public class BeanDependencyInjectionException extends BeanException {
    
    private final @NotBlank String beanName;
    private final @Nullable String propertyName;

    public BeanDependencyInjectionException(@NotBlank String beanName, 
                                           @Nullable String propertyName,
                                           String message) {
        super(buildMessage(beanName, propertyName, message));
        this.beanName = beanName;
        this.propertyName = propertyName;
    }

    public BeanDependencyInjectionException(@NotBlank String beanName, 
                                           @Nullable String propertyName,
                                           String message, 
                                           Throwable cause) {
        super(buildMessage(beanName, propertyName, message), cause);
        this.beanName = beanName;
        this.propertyName = propertyName;
    }

    private static String buildMessage(String beanName, String propertyName, String message) {
        if (propertyName != null) {
            return "Failed to inject dependency for bean '" + beanName + 
                   "' on property '" + propertyName + "': " + message;
        } else {
            return "Failed to inject dependency for bean '" + beanName + "': " + message;
        }
    }
}