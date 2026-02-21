package org.moper.cap.bean.exception;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/**
 * Bean创建异常
 * 在通过构造函数或工厂方法创建Bean实例时发生的异常
 * 这是Bean实例化阶段的核心异常
 */
@Getter
public class BeanCreationException extends BeanException {
    
    private final String beanName;

    public BeanCreationException(@NotBlank String beanName, String message) {
        super("Error creating bean with name '" + beanName + "': " + message);
        this.beanName = beanName;
    }

    public BeanCreationException(@NotBlank String beanName, String message, Throwable cause) {
        super("Error creating bean with name '" + beanName + "': " + message, cause);
        this.beanName = beanName;
    }

}