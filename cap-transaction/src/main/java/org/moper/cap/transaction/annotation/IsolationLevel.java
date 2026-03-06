package org.moper.cap.transaction.annotation;

import static java.sql.Connection.*;

/**
 * 事务隔离级别
 */
public enum IsolationLevel {
    /**
     * 脏读、不可重复读、幻读都会发生
     */
    READ_UNCOMMITTED(TRANSACTION_READ_UNCOMMITTED),

    /**
     * 可防止脏读，但仍会发生不可重复读和幻读
     */
    READ_COMMITTED(TRANSACTION_READ_COMMITTED),

    /**
     * 可防止脏读和不可重复读，但仍会发生幻读
     */
    REPEATABLE_READ(TRANSACTION_REPEATABLE_READ),

    /**
     * 最高隔离级别，可防止脏读、不可重复读和幻读
     */
    SERIALIZABLE(TRANSACTION_SERIALIZABLE);

    public final int level;

    IsolationLevel(int level) {
        this.level = level;
    }
}
