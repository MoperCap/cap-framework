package org.moper.cap.web.embedded;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.StandardRoot;
import org.moper.cap.web.dispatcher.DispatcherServlet;

/**
 * 内嵌 Tomcat 服务器
 *
 * 职责：
 * 1. 创建和配置 Tomcat 实例
 * 2. 注册 DispatcherServlet
 * 3. 启动和关闭服务器
 */
@Slf4j
public class EmbeddedTomcatServer {

    private final DispatcherServlet dispatcherServlet;
    private final int port;
    private final String contextPath;
    private final String baseDir;
    private final int connectionTimeout;
    private final int maxConnections;
    private final int maxThreads;

    private Tomcat tomcat;

    /**
     * 构造函数 - 所有参数从属性系统中获取
     *
     * @param dispatcherServlet DispatcherServlet 实例
     * @param port 服务器端口
     * @param contextPath 应用上下文路径
     * @param baseDir 工作目录
     * @param connectionTimeout 连接超时时间
     * @param maxConnections 最大连接数
     * @param maxThreads 最大线程数
     */
    public EmbeddedTomcatServer(DispatcherServlet dispatcherServlet,
                                int port,
                                String contextPath,
                                String baseDir,
                                int connectionTimeout,
                                int maxConnections,
                                int maxThreads) {
        if (dispatcherServlet == null) {
            throw new IllegalArgumentException("DispatcherServlet cannot be null");
        }
        this.dispatcherServlet = dispatcherServlet;
        this.port = port;
        this.contextPath = contextPath != null ? contextPath : TomcatServerConfig.DEFAULT_CONTEXT_PATH;
        this.baseDir = baseDir != null ? baseDir : TomcatServerConfig.DEFAULT_BASE_DIR;
        this.connectionTimeout = connectionTimeout > 0 ? connectionTimeout : TomcatServerConfig.DEFAULT_CONNECTION_TIMEOUT;
        this.maxConnections = maxConnections > 0 ? maxConnections : TomcatServerConfig.DEFAULT_MAX_CONNECTIONS;
        this.maxThreads = maxThreads > 0 ? maxThreads : TomcatServerConfig.DEFAULT_MAX_THREADS;
    }

    /**
     * 启动 Tomcat 服务器
     */
    public void start() throws LifecycleException {
        log.info("启动内嵌 Tomcat 服务器...");

        // 1. 创建 Tomcat 实例
        tomcat = new Tomcat();
        tomcat.setBaseDir(baseDir);

        // 2. 配置连接器
        Connector connector = new Connector("HTTP/1.1");
        connector.setPort(port);
        connector.setProperty("connectionTimeout", String.valueOf(connectionTimeout));
        connector.setProperty("maxConnections", String.valueOf(maxConnections));
        connector.setProperty("maxThreads", String.valueOf(maxThreads));
        tomcat.setConnector(connector);

        log.info("配置连接器: port={}, maxConnections={}, maxThreads={}",
                port, maxConnections, maxThreads);

        // 3. 创建应用上下文（规范化 contextPath）
        java.io.File appBaseDir = new java.io.File(baseDir, "webapps");
        if (!appBaseDir.exists()) {
            appBaseDir.mkdirs();
        }
        String appBase = appBaseDir.getAbsolutePath();
        String normalizedContextPath = normalizeContextPath(contextPath);
        Context context = tomcat.addContext(normalizedContextPath, appBase);
        context.setReloadable(false);

        log.info("创建应用上下文: contextPath='{}' -> '{}'", contextPath, normalizedContextPath);

        // 4. 配置资源
        StandardRoot standardRoot = new StandardRoot(context);
        context.setResources(standardRoot);

        // 5. 注册 DispatcherServlet
        String servletName = "dispatcher";
        tomcat.addServlet(normalizedContextPath, servletName, dispatcherServlet);
        context.addServletMappingDecoded("/*", servletName);

        log.info("注册 DispatcherServlet: contextPath='{}', mapping=/*", normalizedContextPath);

        // 6. 启动 Tomcat
        tomcat.start();

        log.info("Tomcat 服务器启动成功！");
        log.info("访问地址: http://localhost:{}{}", port,
                "".equals(normalizedContextPath) ? "/" : normalizedContextPath);
    }

    /**
     * 关闭 Tomcat 服务器
     */
    public void stop() throws LifecycleException {
        if (tomcat != null) {
            log.info("关闭 Tomcat 服务器...");
            tomcat.stop();
            tomcat.destroy();
            log.info("Tomcat 服务器已关闭");
        }
    }

    /**
     * 规范化 Tomcat 上下文路径
     *
     * Tomcat 要求上下文路径必须满足：
     * 1. 要么是空字符串（表示根路径）
     * 2. 要么以 '/' 开头且不能以 '/' 结尾
     *
     * @param contextPath 原始上下文路径
     * @return 规范化后的路径
     */
    private String normalizeContextPath(String contextPath) {
        if (contextPath == null || contextPath.isEmpty()) {
            return "";
        }
        // Strip all trailing slashes
        String normalized = contextPath.replaceAll("/+$", "");
        // An empty string (including the case where input was "/" or "///") means root context
        return normalized;
    }

    /**
     * 获取 Tomcat 实例
     */
    public Tomcat getTomcat() {
        return tomcat;
    }

    /**
     * 服务器是否已启动
     */
    public boolean isRunning() {
        return tomcat != null && tomcat.getServer().getState().isAvailable();
    }
}
