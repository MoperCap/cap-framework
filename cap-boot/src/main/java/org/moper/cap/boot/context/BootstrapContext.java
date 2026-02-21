package org.moper.cap.boot.context;


import org.moper.cap.core.ApplicationContext;
import org.moper.cap.core.context.BootstrapLifecycleContext;
import org.moper.cap.core.exception.CapFrameworkException;

/**
 * 框架初始化阶段系统上下文 </br>
 * 仅在框架初始化阶段存在
 */
public interface BootstrapContext extends BootstrapLifecycleContext {

    /**
     * 构建最终的 ApplicationContext </br>
     * 调用后, BootstrapContext 进入不可用状态 </br>
     * 若仍然强行调用，则行为未定义
     *
     * @exception CapFrameworkException 若构建失败，则抛出异常
     */
    ApplicationContext build() throws CapFrameworkException;
}
