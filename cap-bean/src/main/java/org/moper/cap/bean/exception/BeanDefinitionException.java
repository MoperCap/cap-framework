package org.moper.cap.bean.exception;

/**
 * Bean定义异常基类
 * 与Bean定义的注册、查询、验证相关的异常
 */
public class BeanDefinitionException extends BeanException {
    
    public BeanDefinitionException(String message) {
        super(message);
    }

    public BeanDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }
}