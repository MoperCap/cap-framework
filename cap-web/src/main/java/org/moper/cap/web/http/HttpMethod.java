package org.moper.cap.web.http;

/**
 * HTTP 请求方法枚举，涵盖所有标准 HTTP 方法
 *
 * <p>使用示例：
 * <pre>
 * {@code
 * @RequestMapping(path = "/users", method = HttpMethod.GET)
 * public List<User> getUsers() { ... }
 * }
 * </pre>
 */
public enum HttpMethod {

    /**
     * HTTP GET 方法，用于获取资源
     */
    GET,

    /**
     * HTTP POST 方法，用于创建资源
     */
    POST,

    /**
     * HTTP PUT 方法，用于完整替换资源
     */
    PUT,

    /**
     * HTTP DELETE 方法，用于删除资源
     */
    DELETE,

    /**
     * HTTP PATCH 方法，用于部分更新资源
     */
    PATCH,

    /**
     * HTTP HEAD 方法，用于获取资源的头部信息
     */
    HEAD,

    /**
     * HTTP OPTIONS 方法，用于获取服务器支持的 HTTP 方法列表
     */
    OPTIONS,

    /**
     * HTTP TRACE 方法，用于回显服务器收到的请求内容（调试用途）
     */
    TRACE,

    /**
     * HTTP CONNECT 方法，用于建立到服务器的隧道连接（通常用于 HTTPS 代理）
     */
    CONNECT
}
