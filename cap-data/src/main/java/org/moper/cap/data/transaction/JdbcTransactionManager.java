package org.moper.cap.data.transaction;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.transaction.annotation.IsolationLevel;
import org.moper.cap.transaction.context.TransactionContext;
import org.moper.cap.transaction.manager.TransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * JDBC 事务管理器实现 - 由 cap-data 模块提供
 *
 * <p>这是 {@link TransactionManager} 接口的具体实现，
 * 使用 {@link DataSource} 和 JDBC {@link Connection} 来管理事务。
 *
 * <p>特性：
 * <ul>
 *   <li>支持嵌套事务（通过 {@link TransactionContext} 的 Stack 实现）</li>
 *   <li>支持事务隔离级别配置</li>
 *   <li>支持只读事务</li>
 * </ul>
 */
@Slf4j
public class JdbcTransactionManager implements TransactionManager {

    private final DataSource dataSource;

    public JdbcTransactionManager(DataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("DataSource cannot be null");
        }
        this.dataSource = dataSource;
    }

    @Override
    public Connection beginTransaction(boolean readOnly, IsolationLevel isolationLevel) throws Exception {
        TransactionContext.TransactionInfo currentTx = TransactionContext.getCurrentTransaction();

        if (currentTx != null) {
            // 已在事务中，返回当前连接（支持嵌套事务）
            log.debug("已在事务中，复用当前连接，深度: {}", TransactionContext.getTransactionDepth());
            TransactionContext.beginTransaction(currentTx.getConnection(), false, readOnly, isolationLevel.level);
            return currentTx.getConnection();
        }

        // 创建新连接
        Connection connection = dataSource.getConnection();

        try {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(isolationLevel.level);
            connection.setReadOnly(readOnly);
            log.debug("开始新事务: isolationLevel={}, readOnly={}", isolationLevel, readOnly);
        } catch (SQLException e) {
            try {
                connection.close();
            } catch (SQLException closeEx) {
                log.warn("关闭连接失败", closeEx);
            }
            throw new RuntimeException("Failed to configure transaction", e);
        }

        TransactionContext.beginTransaction(connection, true, readOnly, isolationLevel.level);
        return connection;
    }

    @Override
    public void commit(Connection connection) throws Exception {
        TransactionContext.TransactionInfo txInfo = TransactionContext.getCurrentTransaction();

        if (txInfo == null || txInfo.getConnection() != connection) {
            log.warn("无效的提交操作：连接不匹配或无事务上下文");
            return;
        }

        if (!txInfo.isNew()) {
            log.debug("嵌套事务，不实际提交");
            TransactionContext.endTransaction();
            return;
        }

        try {
            connection.commit();
            log.debug("事务已提交");
        } catch (SQLException e) {
            log.error("提交事务失败", e);
            throw new RuntimeException("Failed to commit transaction", e);
        } finally {
            TransactionContext.endTransaction();
            closeConnection(connection);
        }
    }

    @Override
    public void rollback(Connection connection) throws Exception {
        TransactionContext.TransactionInfo txInfo = TransactionContext.getCurrentTransaction();

        if (txInfo == null || txInfo.getConnection() != connection) {
            log.warn("无效的回滚操作：连接不匹配或无事务上下文");
            return;
        }

        if (!txInfo.isNew()) {
            log.debug("嵌套事务，不实际回滚");
            TransactionContext.endTransaction();
            return;
        }

        try {
            connection.rollback();
            log.debug("事务已回滚");
        } catch (SQLException e) {
            log.error("回滚事务失败", e);
            throw new RuntimeException("Failed to rollback transaction", e);
        } finally {
            TransactionContext.endTransaction();
            closeConnection(connection);
        }
    }

    /**
     * 获取当前连接。
     *
     * <p>若当前线程存在活跃事务，返回事务连接（由事务管理器负责关闭）。
     * 否则，返回一个启用自动提交的新连接——<b>调用方负责在使用完毕后关闭该连接</b>，
     * 以避免连接泄漏。
     */
    @Override
    public Connection getCurrentConnection() throws Exception {
        TransactionContext.TransactionInfo txInfo = TransactionContext.getCurrentTransaction();
        if (txInfo != null) {
            return txInfo.getConnection();
        }

        // 如果不在事务中，返回新连接（自动提交模式）
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(true);
        return connection;
    }

    private void closeConnection(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            log.warn("关闭连接失败", e);
        }
    }
}
