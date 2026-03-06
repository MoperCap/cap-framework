package org.moper.cap.transaction.manager;

import org.moper.cap.transaction.annotation.IsolationLevel;

import java.sql.Connection;
import java.sql.Savepoint;

/**
 * 事务管理器接口，定义事务生命周期的标准操作。
 *
 * <p>具体实现由各数据访问模块提供（例如 {@code cap-data} 中的 {@code JdbcTransactionManager}）。
 * cap-transaction 模块只依赖本接口，不依赖任何具体实现，从而保持低耦合。
 *
 * <p>Savepoint 相关方法提供了默认实现，会抛出 {@link UnsupportedOperationException}；
 * 支持 Savepoint 的实现应覆盖这三个方法。
 */
public interface TransactionManager {

    /**
     * 开始一个事务，返回关联的数据库连接。
     *
     * @param readOnly       是否为只读事务
     * @param isolationLevel 事务隔离级别
     * @return 关联的 {@link Connection}，调用方不应手动关闭
     * @throws Exception 开启事务失败
     */
    Connection beginTransaction(boolean readOnly, IsolationLevel isolationLevel) throws Exception;

    /**
     * 提交事务。
     *
     * @param connection 由 {@link #beginTransaction} 返回的连接
     * @throws Exception 提交失败
     */
    void commit(Connection connection) throws Exception;

    /**
     * 回滚事务。
     *
     * @param connection 由 {@link #beginTransaction} 返回的连接
     * @throws Exception 回滚失败
     */
    void rollback(Connection connection) throws Exception;

    /**
     * 获取当前线程绑定的数据库连接。
     *
     * <p>若当前线程存在活跃事务，返回事务连接（由事务管理器负责关闭）。
     * 否则，返回一个启用自动提交的新连接——<b>调用方负责在使用完毕后关闭该连接</b>，
     * 以避免连接泄漏。
     *
     * @return 当前线程的数据库连接
     * @throws Exception 获取连接失败
     */
    Connection getCurrentConnection() throws Exception;

    /**
     * 在当前连接上创建 Savepoint。
     *
     * <p>默认抛出 {@link UnsupportedOperationException}；支持 Savepoint 的实现需覆盖此方法。
     *
     * @param connection 活跃事务连接
     * @return 创建的 {@link Savepoint}
     * @throws Exception 创建失败
     */
    default Savepoint createSavepoint(Connection connection) throws Exception {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " does not support Savepoints");
    }

    /**
     * 将事务回滚到指定的 Savepoint，但保留 Savepoint 之后的外层事务不变。
     *
     * @param connection 活跃事务连接
     * @param savepoint  目标 Savepoint
     * @throws Exception 回滚失败
     */
    default void rollbackToSavepoint(Connection connection, Savepoint savepoint) throws Exception {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " does not support Savepoints");
    }

    /**
     * 释放指定 Savepoint，释放后该 Savepoint 不可再使用。
     *
     * @param connection 活跃事务连接
     * @param savepoint  目标 Savepoint
     * @throws Exception 释放失败
     */
    default void releaseSavepoint(Connection connection, Savepoint savepoint) throws Exception {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " does not support Savepoints");
    }
}
