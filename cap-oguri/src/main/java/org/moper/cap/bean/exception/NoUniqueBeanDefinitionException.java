package org.moper.cap.bean.exception;

import lombok.Getter;

/**
 * 按类型查找 Bean 时，存在多个匹配且无 {@code primary} 标记时抛出。
 */
@Getter
public class NoUniqueBeanDefinitionException extends NoSuchBeanDefinitionException {

    private final int numberOfBeansFound;

    public NoUniqueBeanDefinitionException(Class<?> type, String... beanNamesFound) {
        super(type, "expected single matching bean but found "
                + beanNamesFound.length + ": "
                + String.join(", ", beanNamesFound));
        this.numberOfBeansFound = beanNamesFound.length;
    }

}