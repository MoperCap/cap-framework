package org.moper.cap.bean.aware;

import jakarta.validation.constraints.NotNull;
import org.moper.cap.bean.factory.BeanFactory;

/**
 * Bean工厂感知接口
 * 实现此接口的Bean可以获取到BeanFactory的引用
 */
public interface BeanFactoryAware {
    /**
     * 设置Bean工厂
     *
     * @param beanFactory Bean工厂
     * @throws BeanException 若设置失败
     */
    void setBeanFactory(@NotNull BeanFactory beanFactory) throws BeanException;
}
