package org.moper.cap.web.embedded;

/**
 * Tomcat 服务器配置默认值常量
 */
public class TomcatServerConfig {

    /**
     * 默认服务器端口
     */
    public static final int DEFAULT_PORT = 8080;

    /**
     * 默认应用上下文路径
     */
    public static final String DEFAULT_CONTEXT_PATH = "/";

    /**
     * 默认服务器名称
     */
    public static final String DEFAULT_SERVER_NAME = "cap-web";

    /**
     * 默认工作目录
     */
    public static final String DEFAULT_BASE_DIR = "target/tomcat";

    /**
     * 默认连接超时时间（毫秒）
     */
    public static final int DEFAULT_CONNECTION_TIMEOUT = 20000;

    /**
     * 默认最大连接数
     */
    public static final int DEFAULT_MAX_CONNECTIONS = 200;

    /**
     * 默认最大线程数
     */
    public static final int DEFAULT_MAX_THREADS = 10;

    private TomcatServerConfig() {
    }
}
