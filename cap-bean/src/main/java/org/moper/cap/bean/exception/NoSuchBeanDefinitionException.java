package org.moper.cap.bean.exception;

public class NoSuchBeanDefinitionException extends BeanException {
    public NoSuchBeanDefinitionException(String message) {
        super(message);
    }

    public NoSuchBeanDefinitionException(String message, Throwable cause) { super(message, cause); }
}
