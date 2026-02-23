package org.moper.cap.context;

import org.moper.cap.exception.ContextException;

/**
 * 框架初始化阶段系统上下文 </br>
 * 仅在框架初始化阶段存在
 */
public interface BootstrapContext{

    /**
     * 构建最终的 ApplicationContext </br>
     * 调用后, BootstrapContext 进入不可用状态 </br>
     * 若仍然强行调用，则行为未定义 </br>
     *
     * @param factory 根据BootstrapContext构建ApplicationContext的工厂策略
     * @return 构建完成的ApplicationContext实例
     * @param <T> ApplicationContext的具体类型
     * @throws ContextException 若构建失败，则抛出异常
     */
    default  <T extends ApplicationContext> T build(ApplicationContextFactory<T> factory) throws ContextException {
        return factory.create(this);
    }
}
