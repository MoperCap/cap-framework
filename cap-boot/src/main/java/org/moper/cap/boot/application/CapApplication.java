package org.moper.cap.boot.application;

import org.moper.cap.core.context.ApplicationContext;
import org.moper.cap.core.exception.CapFrameworkException;

/**
 * 框架启动类接口
 */
public interface CapApplication extends AutoCloseable{

    /**
     * 启动框架
     *
     * @throws CapFrameworkException 若启动框架失败，则抛出异常
     */
    ApplicationContext run() throws CapFrameworkException;

    default void close() throws CapFrameworkException {
        // do nothing
    }
}
