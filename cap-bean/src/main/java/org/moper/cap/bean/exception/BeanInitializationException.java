package org.moper.cap.bean.exception;

import jakarta.validation.constraints.NotBlank;

/**
 * Bean初始化异常
 * 在执行Bean的初始化方法时发生的异常
 * 包括：
 * - InitializingBean.afterPropertiesSet() 方法
 * - 自定义的init方法（通过BeanDefinition.initMethodName指定）
 */
public class BeanInitializationException extends BeanLifecycleException {
    
    public BeanInitializationException(@NotBlank String beanName, String message) {
        super(beanName, LifecyclePhase.INITIALIZATION, message);
    }

    public BeanInitializationException(@NotBlank String beanName, 
                                      String message, 
                                      Throwable cause) {
        super(beanName, LifecyclePhase.INITIALIZATION, message, cause);
    }

    public BeanInitializationException(@NotBlank String beanName, Throwable cause) {
        super(beanName, LifecyclePhase.INITIALIZATION, "Initialization method threw exception", cause);
    }
}