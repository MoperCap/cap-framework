package org.moper.cap.bean.exception;

import jakarta.validation.constraints.NotBlank;

/**
 * Bean销毁异常
 * 在执行Bean的销毁方法时发生的异常
 * 包括：
 * - DisposableBean.destroy() 方法
 * - 自定义的destroy方法（通过BeanDefinition.destroyMethodName指定）
 */
public class BeanDestructionException extends BeanLifecycleException {
    
    public BeanDestructionException(@NotBlank String beanName, String message) {
        super(beanName, LifecyclePhase.DESTRUCTION, message);
    }

    public BeanDestructionException(@NotBlank String beanName, 
                                   String message, 
                                   Throwable cause) {
        super(beanName, LifecyclePhase.DESTRUCTION, message, cause);
    }

    public BeanDestructionException(@NotBlank String beanName, Throwable cause) {
        super(beanName, LifecyclePhase.DESTRUCTION, "Destruction method threw exception", cause);
    }
}