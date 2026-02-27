package org.moper.cap.bean.exception;

/**
 * 与 BeanDefinition 的注册、查询、操作相关的异常基类。
 */
public class BeanDefinitionException extends BeanException {

    public BeanDefinitionException(String message) {
        super(message);
    }

    public BeanDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }
}