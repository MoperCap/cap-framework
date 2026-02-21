package org.moper.cap.bean.exception;

import jakarta.validation.constraints.NotBlank;

/**
 * Bean循环依赖异常
 * 检测到Bean之间存在无法解决的循环依赖关系
 */
public class BeanCurrentlyInCreationException extends BeanCreationException {
    
    public BeanCurrentlyInCreationException(@NotBlank String beanName) {
        super(beanName, "Requested bean is currently in creation: " +
              "Is there an unresolvable circular reference?");
    }

    public BeanCurrentlyInCreationException(@NotBlank String beanName, String message) {
        super(beanName, message);
    }
}