package org.moper.cap.context.runner;

import org.moper.cap.context.context.BootstrapContext;

/**
 * 框架初始化阶段执行器 </br>
 *
 * 负责在框架启动阶段执行相关逻辑 </br>
 * 例如：构造机、资源加载器、环境准备器等
 */
public non-sealed interface BootstrapRunner extends AutoCloseable, Runner {

    /**
     * 框架初始化阶段执行器 </br>
     *
     * @param context 框架初始化阶段系统上下文
     * @throws Exception 执行过程中可能抛出的异常
     */
    void initialize(BootstrapContext context) throws Exception;
}
