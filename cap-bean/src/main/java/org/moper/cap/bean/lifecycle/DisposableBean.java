package org.moper.cap.bean.lifecycle;


import org.moper.cap.bean.exception.BeanException;

/**
 * Bean销毁接口
 * 在Bean销毁前调用
 */
public interface DisposableBean {
    /**
     * 在Bean销毁前调用
     *
     * @throws BeanException 若销毁失败
     */
    void destroy() throws BeanException;
}
