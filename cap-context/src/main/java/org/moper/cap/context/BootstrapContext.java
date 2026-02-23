package org.moper.cap.context;

import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.bootstrap.Initializer;
import org.moper.cap.environment.Environment;
import org.moper.cap.exception.ContextException;

/**
 * 框架初始化阶段系统上下文 </br>
 * 仅在框架初始化阶段存在
 */
public interface BootstrapContext {

    /**
     * 获取 Bean 容器
     *
     * @return Bean 容器实例
     */
    BeanContainer getBeanContainer();

    /**
     * 获取环境配置
     *
     * @return 环境配置实例
     */
    Environment getEnvironment();

    /**
     * 注册一个 Initializer
     *
     * @param initializer 要注册的构造机
     */
    void addInitializer(Initializer initializer);

    /**
     * 构建最终的 ApplicationContext </br>
     * 调用后, BootstrapContext 进入不可用状态 </br>
     * 若仍然强行调用，则抛出 ContextException </br>
     *
     * @param factory 根据BootstrapContext构建ApplicationContext的工厂策略
     * @return 构建完成的ApplicationContext实例
     * @param <T> ApplicationContext的具体类型
     * @throws ContextException 若构建失败，则抛出异常
     */
    <T extends ApplicationContext> T build(ApplicationContextFactory<T> factory) throws ContextException;
}