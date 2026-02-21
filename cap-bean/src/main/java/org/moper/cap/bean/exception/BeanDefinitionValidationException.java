package org.moper.cap.bean.exception;

import jakarta.validation.constraints.NotBlank;

/**
 * Bean定义验证异常
 * 当Bean定义不符合规范时抛出
 */
public class BeanDefinitionValidationException extends BeanDefinitionException {
    
    public BeanDefinitionValidationException(@NotBlank String beanName, String message) {
        super("Validation failed for bean definition '" + beanName + "': " + message);
    }
}