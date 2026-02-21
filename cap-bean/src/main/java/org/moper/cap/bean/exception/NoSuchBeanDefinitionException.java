package org.moper.cap.bean.exception;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Bean定义不存在异常
 */
public class NoSuchBeanDefinitionException extends BeanDefinitionException {
    
    public NoSuchBeanDefinitionException(@NotBlank String beanName) {
        super("No bean named '" + beanName + "' available");
    }

    public NoSuchBeanDefinitionException(@NotNull Class<?> type) {
        super("No qualifying bean of type '" + type.getName() + "' available");
    }

    public NoSuchBeanDefinitionException(@NotNull Class<?> type, String message) {
        super("No qualifying bean of type '" + type.getName() + "' available: " + message);
    }
}