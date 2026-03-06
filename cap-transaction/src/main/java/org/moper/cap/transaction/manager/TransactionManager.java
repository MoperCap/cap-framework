package org.moper.cap.transaction.manager;

import org.moper.cap.transaction.annotation.IsolationLevel;

import java.sql.Connection;

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
}
