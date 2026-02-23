package org.moper.cap.bean.exception;

/**
 * Bean 的初始化回调（{@link org.moper.cap.bean.lifecycle.BeanLifecycle#afterPropertiesSet()}）
 * 执行失败时抛出。
 *
 * <p>是 {@link BeanCreationException} 在初始化阶段的具体化。
 */
public class BeanInitializationException extends BeanCreationException {

    public BeanInitializationException(String beanName, Throwable cause) {
        super(beanName, "afterPropertiesSet() threw an exception", cause);
    }
}