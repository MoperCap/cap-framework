package org.moper.cap.bean.exception;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/**
 * Bean Aware接口处理异常
 * 在调用Aware接口方法时发生的异常
 * 这是一个功能性异常，不属于生命周期范畴
 */
@Getter
public class BeanAwareProcessException extends BeanException {
    
    private final String beanName;
    private final Class<?> awareInterface;

    public BeanAwareProcessException(@NotBlank String beanName, 
                                    @NotNull Class<?> awareInterface, 
                                    String message) {
        super("Failed to invoke " + awareInterface.getSimpleName() + 
              " on bean '" + beanName + "': " + message);
        this.beanName = beanName;
        this.awareInterface = awareInterface;
    }

    public BeanAwareProcessException(@NotBlank String beanName, 
                                    @NotNull Class<?> awareInterface, 
                                    String message, 
                                    Throwable cause) {
        super("Failed to invoke " + awareInterface.getSimpleName() + 
              " on bean '" + beanName + "': " + message, cause);
        this.beanName = beanName;
        this.awareInterface = awareInterface;
    }

    public BeanAwareProcessException(@NotBlank String beanName, 
                                    @NotNull Class<?> awareInterface, 
                                    Throwable cause) {
        super("Failed to invoke " + awareInterface.getSimpleName() + 
              " on bean '" + beanName + "'", cause);
        this.beanName = beanName;
        this.awareInterface = awareInterface;
    }

}