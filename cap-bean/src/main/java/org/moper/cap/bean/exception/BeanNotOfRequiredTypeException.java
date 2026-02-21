package org.moper.cap.bean.exception;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Bean类型不匹配异常
 */
public class BeanNotOfRequiredTypeException extends BeanDefinitionException {
    
    public BeanNotOfRequiredTypeException(@NotBlank String beanName, 
                                         @NotNull Class<?> requiredType, 
                                         @NotNull Class<?> actualType) {
        super("Bean named '" + beanName + "' is expected to be of type '" + 
              requiredType.getName() + "' but was actually of type '" + 
              actualType.getName() + "'");
    }
}