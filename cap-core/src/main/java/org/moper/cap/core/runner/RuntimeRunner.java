package org.moper.cap.core.runner;

import org.moper.cap.core.context.RuntimeContext;

/**
 * 框架运行阶段执行器 </br>
 *
 * 负责在框架运行阶段执行相关逻辑 </br>
 * 例如：应用启动器、应用关闭器等
 */
public non-sealed interface RuntimeRunner extends Runner {

    /**
     * 应用启动阶段执行器 </br>
     *
     * 应用启动阶段执行器负责在框架运行阶段执行应用启动相关逻辑 </br>
     * @param context 框架运行阶段系统上下文 </br>
     * @throws Exception 执行过程中可能抛出的异常
     */
    void onApplicationStarted(RuntimeContext context) throws Exception;

    /**
     * 应用关闭阶段执行器 </br>
     *
     * 应用关闭阶段执行器负责在框架运行阶段执行应用关闭相关逻辑 </br>
     * @throws Exception 执行过程中可能抛出的异常
     */
    void onApplicationClosed() throws Exception;
}
