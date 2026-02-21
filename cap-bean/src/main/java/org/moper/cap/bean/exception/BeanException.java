package org.moper.cap.bean.exception;

/**
 * Bean相关异常基类
 * 所有Bean模块异常的根异常
 */
public class BeanException extends Exception {
    
    public BeanException(String message) {
        super(message);
    }

    public BeanException(String message, Throwable cause) {
        super(message, cause);
    }
}