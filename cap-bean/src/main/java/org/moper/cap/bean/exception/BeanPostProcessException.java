package org.moper.cap.bean.exception;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/**
 * Bean后置处理异常
 * 在BeanPostProcessor处理过程中发生的异常
 * 这是一个功能性异常，不属于生命周期范畴
 */
@Getter
public class BeanPostProcessException extends BeanException {
    
    private final String beanName;
    private final String postProcessorName;

    public BeanPostProcessException(@NotBlank String beanName, 
                                   @NotBlank String postProcessorName, 
                                   String message) {
        super("Post-processing of bean '" + beanName + 
              "' by '" + postProcessorName + "' failed: " + message);
        this.beanName = beanName;
        this.postProcessorName = postProcessorName;
    }

    public BeanPostProcessException(@NotBlank String beanName, 
                                   @NotBlank String postProcessorName, 
                                   String message, 
                                   Throwable cause) {
        super("Post-processing of bean '" + beanName + 
              "' by '" + postProcessorName + "' failed: " + message, cause);
        this.beanName = beanName;
        this.postProcessorName = postProcessorName;
    }

    public BeanPostProcessException(@NotBlank String beanName, 
                                   @NotBlank String postProcessorName, 
                                   Throwable cause) {
        super("Post-processing of bean '" + beanName + 
              "' by '" + postProcessorName + "' failed", cause);
        this.beanName = beanName;
        this.postProcessorName = postProcessorName;
    }

}