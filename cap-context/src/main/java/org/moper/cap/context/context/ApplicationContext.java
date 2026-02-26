package org.moper.cap.context.context;

import org.moper.cap.bean.container.BeanInspector;
import org.moper.cap.bean.container.BeanProvider;
import org.moper.cap.context.exception.ContextException;
import org.moper.cap.property.officer.PropertyOfficer;

/**
 * 框架运行期系统上下文。
 *
 * 典型用法：
 * try (ApplicationContext context =
 *         new DefaultBootstrapContext(AppConfig.class, args)
 *             .build(DefaultApplicationContextFactory.INSTANCE)) {
 *     context.run();
 * }
 */
public interface ApplicationContext extends BeanProvider, BeanInspector, AutoCloseable {

    /**
     * 启动运行期：
     * 1. preInstantiateSingletons()
     * 2. 注册 JVM shutdown hook
     * 幂等，多次调用安全。
     */
    void run() throws ContextException;

    /**
     * 获取属性管理平台
     */
    PropertyOfficer getPropertyOfficer();
}