package org.moper.cap.bean.exception;

import jakarta.validation.constraints.NotBlank;

/**
 * Bean实例化失败异常
 * 构造函数或工厂方法调用成功找到，但执行时抛出异常
 */
public class BeanInstantiationFailedException extends BeanCreationException {
    
    public BeanInstantiationFailedException(@NotBlank String beanName, 
                                           String message, 
                                           Throwable cause) {
        super(beanName, "Failed to instantiate: " + message, cause);
    }

    public BeanInstantiationFailedException(@NotBlank String beanName, 
                                           Throwable cause) {
        super(beanName, "Failed to instantiate", cause);
    }
}