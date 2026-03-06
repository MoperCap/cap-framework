package org.moper.cap.transaction.manager;

import org.moper.cap.transaction.annotation.IsolationLevel;

import java.sql.Connection;
import java.sql.Savepoint;

/**
 * 事务管理器接口
 */
public interface TransactionManager {

    /**
     * 开始事务
     *
     * @param readOnly       是否为只读事务
     * @param isolationLevel 隔离级别
     * @return 事务连接
     */
    Connection beginTransaction(boolean readOnly, IsolationLevel isolationLevel) throws Exception;

    /**
     * 提交事务
     */
    void commit(Connection connection) throws Exception;

    /**
     * 回滚事务
     */
    void rollback(Connection connection) throws Exception;

    /**
     * 获取当前连接
     */
    Connection getCurrentConnection() throws Exception;

    /**
     * 为嵌套事务（NESTED 传播性）在指定连接上创建 Savepoint。
     *
     * <p>默认实现抛出 {@link UnsupportedOperationException}，子类按需覆盖。
     *
     * @param connection 当前事务连接
     * @return 创建的 {@link Savepoint}
     */
    default Savepoint createSavepoint(Connection connection) throws Exception {
        throw new UnsupportedOperationException("Savepoints are not supported by this TransactionManager");
    }

    /**
     * 将事务回滚至指定 Savepoint（NESTED 传播性失败回滚）。
     *
     * <p>默认实现抛出 {@link UnsupportedOperationException}，子类按需覆盖。
     *
     * @param connection 当前事务连接
     * @param savepoint  目标 Savepoint
     */
    default void rollbackToSavepoint(Connection connection, Savepoint savepoint) throws Exception {
        throw new UnsupportedOperationException("Savepoints are not supported by this TransactionManager");
    }

    /**
     * 释放指定 Savepoint（NESTED 传播性成功后清理）。
     *
     * <p>默认实现抛出 {@link UnsupportedOperationException}，子类按需覆盖。
     * 部分数据库不支持此操作，实现类可静默忽略该错误。
     *
     * @param connection 当前事务连接
     * @param savepoint  要释放的 Savepoint
     */
    default void releaseSavepoint(Connection connection, Savepoint savepoint) throws Exception {
        throw new UnsupportedOperationException("Savepoints are not supported by this TransactionManager");
    }
}
