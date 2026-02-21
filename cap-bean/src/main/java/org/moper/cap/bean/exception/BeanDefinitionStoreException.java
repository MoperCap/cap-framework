package org.moper.cap.bean.exception;

/**
 * Bean定义存储异常
 * 在注册、移除Bean定义时发生的异常
 */
public class BeanDefinitionStoreException extends BeanDefinitionException {
    
    public BeanDefinitionStoreException(String message) {
        super(message);
    }

    public BeanDefinitionStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}