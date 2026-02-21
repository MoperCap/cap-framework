package org.moper.cap.bean.lifecycle;


import org.moper.cap.bean.exception.BeanException;

/**
 * Bean初始化接口
 * 在Bean的所有属性设置完成后调用
 */
public interface InitializingBean {
    /**
     * 在Bean的所有属性设置完成后调用
     *
     * @throws BeanException 若初始化失败
     */
    void afterPropertiesSet() throws BeanException;
}
