package org.moper.cap.bean.lifecycle;


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
