package org.moper.cap.bean.exception;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/**
 * Bean生命周期异常
 * 在Bean的生命周期方法（初始化方法、销毁方法）执行时发生的异常
 */
@Getter
public class BeanLifecycleException extends BeanException {
    
    private final String beanName;
    private final LifecyclePhase phase;

    public BeanLifecycleException(@NotBlank String beanName, 
                                 @NotNull LifecyclePhase phase, 
                                 String message) {
        super("Bean '" + beanName + "' failed at lifecycle phase [" + phase + "]: " + message);
        this.beanName = beanName;
        this.phase = phase;
    }

    public BeanLifecycleException(@NotBlank String beanName, 
                                 @NotNull LifecyclePhase phase, 
                                 String message, 
                                 Throwable cause) {
        super("Bean '" + beanName + "' failed at lifecycle phase [" + phase + "]: " + message, cause);
        this.beanName = beanName;
        this.phase = phase;
    }

    /**
     * Bean生命周期阶段枚举
     */
    public enum LifecyclePhase {
        /**
         * 初始化阶段（InitializingBean.afterPropertiesSet() 或自定义init方法）
         */
        INITIALIZATION,
        
        /**
         * 销毁阶段（DisposableBean.destroy() 或自定义destroy方法）
         */
        DESTRUCTION
    }
}