package org.moper.cap.bean.exception;

import jakarta.validation.constraints.NotBlank;

/**
 * 工厂Bean未找到异常
 * 当使用实例工厂方法创建Bean时，指定的工厂Bean在容器中不存在
 */
public class FactoryBeanNotFoundException extends BeanCreationException {
    
    public FactoryBeanNotFoundException(@NotBlank String beanName, 
                                       @NotBlank String factoryBeanName) {
        super(beanName, "Factory bean '" + factoryBeanName + "' not found in container");
    }
}