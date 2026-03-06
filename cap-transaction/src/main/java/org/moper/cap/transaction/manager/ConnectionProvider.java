package org.moper.cap.transaction.manager;

import java.sql.Connection;

/**
 * 数据库连接提供者 SPI
 *
 * <p>定义获取和释放数据库连接的标准接口，将连接获取策略从事务管理逻辑中解耦。
 * 不同实现可支持直接 DataSource、连接池等多种连接方式。
 *
 * <p>JDBC 实现由 {@code cap-data} 模块提供（{@code DataSourceConnectionProvider}），
 * 其他数据库中间件亦可提供各自实现。
 *
 * <p>典型使用流程：
 * <pre>{@code
 * Connection conn = connectionProvider.getConnection();
 * try {
 *     // 执行业务逻辑
 * } finally {
 *     connectionProvider.releaseConnection(conn);
 * }
 * }</pre>
 */
public interface ConnectionProvider {

    /**
     * 获取数据库连接。
     *
     * @return 可用的数据库连接
     * @throws Exception 获取连接时发生错误
     */
    Connection getConnection() throws Exception;

    /**
     * 释放之前获取的数据库连接。
     *
     * <p>实现应根据连接来源决定是关闭连接还是将其归还给连接池。
     *
     * @param connection 要释放的连接
     * @throws Exception 释放连接时发生错误
     */
    void releaseConnection(Connection connection) throws Exception;
}
