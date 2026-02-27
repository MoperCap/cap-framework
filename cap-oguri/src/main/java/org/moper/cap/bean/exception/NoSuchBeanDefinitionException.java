package org.moper.cap.bean.exception;

/**
 * 按名称或类型查找 Bean 时，容器中不存在匹配的 Bean 时抛出。
 */
public class NoSuchBeanDefinitionException extends BeanDefinitionException {

    public NoSuchBeanDefinitionException(String beanName) {
        super("No bean named '" + beanName + "' available");
    }

    public NoSuchBeanDefinitionException(Class<?> type) {
        super("No qualifying bean of type '" + type.getName() + "' available");
    }

    public NoSuchBeanDefinitionException(Class<?> type, String message) {
        super("No qualifying bean of type '" + type.getName() + "' available: " + message);
    }
}