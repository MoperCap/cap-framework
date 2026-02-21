package org.moper.cap.bean.exception;

/**
 * Bean工厂配置已冻结异常
 * 当尝试在配置冻结后修改Bean定义时抛出
 */
public class BeanFactoryConfigurationFrozenException extends BeanException {
    
    public BeanFactoryConfigurationFrozenException() {
        super("Bean factory configuration is frozen; no modifications allowed");
    }

    public BeanFactoryConfigurationFrozenException(String operation) {
        super("Bean factory configuration is frozen; cannot perform operation: " + operation);
    }
}