package org.moper.cap.transaction.annotation;

/**
 * 事务隔离级别，与 JDBC {@link java.sql.Connection} 定义的常量对应。
 *
 * <p>使用 {@link #DEFAULT}（{@code -1}）时，框架不修改连接的隔离级别，
 * 直接使用数据源的默认设置。
 */
public enum IsolationLevel {

    /** 使用数据源的默认隔离级别，不主动修改连接隔离级别。 */
    DEFAULT(-1),

    /** 读未提交：允许脏读。对应 {@link java.sql.Connection#TRANSACTION_READ_UNCOMMITTED}。 */
    READ_UNCOMMITTED(1),

    /** 读已提交：禁止脏读，允许不可重复读。对应 {@link java.sql.Connection#TRANSACTION_READ_COMMITTED}。 */
    READ_COMMITTED(2),

    /** 可重复读：禁止脏读和不可重复读，允许幻读。对应 {@link java.sql.Connection#TRANSACTION_REPEATABLE_READ}。 */
    REPEATABLE_READ(4),

    /** 串行化：最高隔离级别，完全串行。对应 {@link java.sql.Connection#TRANSACTION_SERIALIZABLE}。 */
    SERIALIZABLE(8);

    /** JDBC 隔离级别常量值，{@code -1} 表示 DEFAULT。 */
    public final int level;

    IsolationLevel(int level) {
        this.level = level;
    }
}
