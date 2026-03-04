package org.moper.cap.bean.exception;

import lombok.Getter;

/**
 * Bean 存在但实际类型与期望类型不符时抛出。
 */
@Getter
public class BeanNotOfRequiredTypeException extends BeanException {

    private final String   beanName;
    private final Class<?> requiredType;
    private final Class<?> actualType;

    public BeanNotOfRequiredTypeException(String beanName,
                                          Class<?> requiredType,
                                          Class<?> actualType) {
        super("Bean named '" + beanName + "' is expected to be of type '"
                + requiredType.getName() + "' but was actually of type '"
                + actualType.getName() + "'");
        this.beanName     = beanName;
        this.requiredType = requiredType;
        this.actualType   = actualType;
    }

}