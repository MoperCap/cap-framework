package org.moper.cap.bean.exception;

import lombok.Getter;

/**
 * Bean 创建过程中任意阶段（实例化、属性注入、拦截器处理、初始化回调）失败时抛出。
 */
@Getter
public class BeanCreationException extends BeanException {

    private final String beanName;

    public BeanCreationException(String beanName, String message) {
        super("Error creating bean with name '" + beanName + "': " + message);
        this.beanName = beanName;
    }

    public BeanCreationException(String beanName, String message, Throwable cause) {
        super("Error creating bean with name '" + beanName + "': " + message, cause);
        this.beanName = beanName;
    }

}