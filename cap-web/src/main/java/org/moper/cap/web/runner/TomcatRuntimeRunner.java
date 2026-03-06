package org.moper.cap.web.runner;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.moper.cap.core.annotation.RunnerMeta;
import org.moper.cap.core.context.RuntimeContext;
import org.moper.cap.core.runner.RuntimeRunner;
import org.moper.cap.core.runner.RunnerType;
import org.moper.cap.property.officer.PropertyOfficer;
import org.moper.cap.web.binder.ParameterBinderRegistry;
import org.moper.cap.web.dispatcher.DispatcherServlet;
import org.moper.cap.web.embedded.EmbeddedTomcatServer;
import org.moper.cap.web.embedded.TomcatServerConfig;
import org.moper.cap.web.interceptor.InterceptorRegistry;
import org.moper.cap.web.invoker.MethodInvoker;
import org.moper.cap.web.invoker.factory.MethodInvokerFactory;
import org.moper.cap.web.router.RouteRegistry;
import org.moper.cap.web.view.ViewHandlerRegistry;
import org.moper.cap.web.view.support.DefaultViewHandlerRegistry;

/**
 * 运行时启动器 - 启动内嵌 Tomcat 服务器
 *
 * 职责：
 * 1. 从属性系统获取 Tomcat 配置参数
 * 2. 初始化 Web MVC 组件
 * 3. 启动内嵌 Tomcat 服务器
 */
@Getter
@Slf4j
@RunnerMeta(type = RunnerType.FEATURE, order = 300, description = "Starts embedded Tomcat server")
public class TomcatRuntimeRunner implements RuntimeRunner {

    /**
     * -- GETTER --
     *  获取 Tomcat 服务器实例
     */
    private EmbeddedTomcatServer tomcatServer;

    @Override
    public void onApplicationStarted(RuntimeContext context) throws Exception {
        log.info("启动运行时环境...");

        // 1. 从属性系统获取 Tomcat 配置参数
        PropertyOfficer propertyOfficer = context.getPropertyOfficer();
        int port = propertyOfficer.getPropertyValueOrDefault("server.port", Integer.class, TomcatServerConfig.DEFAULT_PORT);
        String contextPath = propertyOfficer.getPropertyValueOrDefault("server.servlet.context-path", String.class, TomcatServerConfig.DEFAULT_CONTEXT_PATH);
        String baseDir = propertyOfficer.getPropertyValueOrDefault("server.tomcat.basedir", String.class, TomcatServerConfig.DEFAULT_BASE_DIR);
        int connectionTimeout = propertyOfficer.getPropertyValueOrDefault("server.tomcat.connection-timeout", Integer.class, TomcatServerConfig.DEFAULT_CONNECTION_TIMEOUT);
        int maxConnections = propertyOfficer.getPropertyValueOrDefault("server.tomcat.max-connections", Integer.class, TomcatServerConfig.DEFAULT_MAX_CONNECTIONS);
        int maxThreads = propertyOfficer.getPropertyValueOrDefault("server.tomcat.threads.max", Integer.class, TomcatServerConfig.DEFAULT_MAX_THREADS);

        log.info("从属性系统读取 Tomcat 配置: port={}, contextPath={}, baseDir={}",
                port, contextPath, baseDir);

        // 2. 初始化 Web MVC 组件
        RouteRegistry routeRegistry = context.getBean("routeRegistry", RouteRegistry.class);
        ParameterBinderRegistry parameterBinderRegistry = context.getBean("parameterBinderRegistry", ParameterBinderRegistry.class);
        MethodInvoker methodInvoker = MethodInvokerFactory.create(parameterBinderRegistry);
        ViewHandlerRegistry viewHandlerRegistry = new DefaultViewHandlerRegistry();
        InterceptorRegistry interceptorRegistry = context.containsBean("interceptorRegistry")
                ? context.getBean("interceptorRegistry", InterceptorRegistry.class)
                : null;

        // 3. 创建和配置 DispatcherServlet
        DispatcherServlet dispatcherServlet = new DispatcherServlet();
        dispatcherServlet.setRouteRegistry(routeRegistry);
        dispatcherServlet.setMethodInvoker(methodInvoker);
        dispatcherServlet.setViewHandlerRegistry(viewHandlerRegistry);
        dispatcherServlet.setInterceptorRegistry(interceptorRegistry);

        log.info("DispatcherServlet 配置完成");

        // 4. 创建内嵌 Tomcat 服务器
        tomcatServer = new EmbeddedTomcatServer(
                dispatcherServlet,
                port,
                contextPath,
                baseDir,
                connectionTimeout,
                maxConnections,
                maxThreads
        );

        // 5. 启动 Tomcat
        tomcatServer.start();

        log.info("运行时环境启动完成");
    }

    @Override
    public void onApplicationClosed() throws Exception {
        if (tomcatServer != null && tomcatServer.isRunning()) {
            try {
                tomcatServer.stop();
            } catch (Exception e) {
                log.error("关闭 Tomcat 失败", e);
            }
        }
    }

}
