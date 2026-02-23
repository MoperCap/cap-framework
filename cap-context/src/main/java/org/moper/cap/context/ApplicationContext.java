package org.moper.cap.context;

import org.moper.cap.bean.container.BeanInspector;
import org.moper.cap.bean.container.BeanProvider;
import org.moper.cap.environment.Environment;
import org.moper.cap.event.ApplicationEvent;
import org.moper.cap.exception.ContextException;

/**
 * 框架运行期阶段系统上下文
 * 提供只读的Bean访问能力
 */
public interface ApplicationContext extends BeanProvider, BeanInspector, AutoCloseable {

    /**
     * 启动应用上下文：预实例化所有单例Bean，注册JVM关闭钩子，发布ContextStartedEvent
     *
     * @throws ContextException 若启动失败
     */
    void run() throws ContextException;

    /**
     * 关闭应用上下文：发布ContextClosedEvent，销毁所有单例Bean
     */
    @Override
    void close();

    /**
     * 获取环境配置
     *
     * @return 环境配置实例
     */
    Environment getEnvironment();

    /**
     * 发布应用事件
     *
     * @param event 要发布的事件
     */
    void publishEvent(ApplicationEvent event);
}
