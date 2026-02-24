package org.moper.cap.context.context;

import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.context.config.ConfigurationClass;
import org.moper.cap.context.environment.Environment;
import org.moper.cap.context.exception.ContextException;

/**
 * 框架初始化完成后的系统上下文。
 * 所有初始化工作在 DefaultBootstrapContext 构造时完成。
 * build() 仅负责将已初始化的 BootstrapContext 转换为 ApplicationContext。
 */
public interface BootstrapContext {

    /** 获取已完成 BeanDefinition 注册的 Bean 容器 */
    BeanContainer getBeanContainer();

    /** 获取已完成属性加载的环境上下文 */
    Environment getEnvironment();

    /** 获取配置类信息视图 */
    ConfigurationClass getConfigurationClass();

    /**
     * 纯转换：将已初始化完成的 BootstrapContext 转换为 ApplicationContext。
     * 不执行任何初始化逻辑。
     */
    <T extends ApplicationContext> T build(ApplicationContextFactory<T> factory)
            throws ContextException;
}