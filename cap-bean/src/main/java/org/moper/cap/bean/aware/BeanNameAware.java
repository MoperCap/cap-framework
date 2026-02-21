package org.moper.cap.bean.aware;

import jakarta.validation.constraints.NotBlank;

/**
 * Bean名称感知接口
 * 实现此接口的Bean可以获取到自己的Bean名称
 */
public interface BeanNameAware {

    /**
     * 设置Bean名称
     *
     * @param beanName Bean名称
     */
    void setBeanName(@NotBlank String beanName);
}
