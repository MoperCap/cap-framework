package org.moper.cap.boot.bootstrap;

import org.moper.cap.context.BootstrapContext;
import org.moper.cap.core.exception.CapFrameworkException;

/**
 * 框架启动阶段构造机
 */
public interface Initializer extends AutoCloseable {
    /**
     * 框架启动阶段执行
     *
     * @param context 初始化上下文
     */
    void initialize(BootstrapContext context) throws CapFrameworkException;

    @Override
    default void close() throws CapFrameworkException {
        // do nothing
    }
}
