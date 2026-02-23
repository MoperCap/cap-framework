package org.moper.cap.bean.exception;

import lombok.Getter;

/**
 * Bean 的销毁回调（{@link org.moper.cap.bean.lifecycle.BeanLifecycle#destroy()}）
 * 执行失败时抛出。
 *
 * <p>与 {@link BeanCreationException} 语义对称，专属于销毁阶段。
 */
@Getter
public class BeanDestructionException extends BeanException {

    private final String beanName;

    public BeanDestructionException(String beanName, Throwable cause) {
        super("Error destroying bean with name '" + beanName + "'", cause);
        this.beanName = beanName;
    }

}