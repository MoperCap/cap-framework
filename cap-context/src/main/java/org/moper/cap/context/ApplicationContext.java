package org.moper.cap.context;

import org.moper.cap.bean.container.BeanInspector;
import org.moper.cap.bean.container.BeanProvider;
import org.moper.cap.environment.Environment;
import org.moper.cap.exception.ContextException;

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

    /** 获取环境上下文 */
    Environment getEnvironment();

    /**
     * 关闭，释放所有资源。幂等。
     * 1. destroySingletons()
     * 2. environment.close()
     */
    @Override
    void close();
}