package org.moper.cap.bean.exception;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/**
 * 存在多个Bean定义异常
 * 当按类型查找Bean时，存在多个符合条件的Bean且没有primary标记
 */
@Getter
public class NoUniqueBeanDefinitionException extends NoSuchBeanDefinitionException {
    
    private final int numberOfBeansFound;
    private final String[] beanNames;

    public NoUniqueBeanDefinitionException(@NotNull Class<?> type, 
                                          int numberOfBeansFound, 
                                          String message) {
        super(type, message);
        this.numberOfBeansFound = numberOfBeansFound;
        this.beanNames = null;
    }

    public NoUniqueBeanDefinitionException(@NotNull Class<?> type, 
                                          String... beanNamesFound) {
        super(type, "expected single matching bean but found " + beanNamesFound.length + 
              ": " + String.join(", ", beanNamesFound));
        this.numberOfBeansFound = beanNamesFound.length;
        this.beanNames = beanNamesFound;
    }

}