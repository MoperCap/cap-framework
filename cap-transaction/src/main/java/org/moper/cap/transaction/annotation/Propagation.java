package org.moper.cap.transaction.annotation;

/**
 * 事务传播性
 */
public enum Propagation {
    /**
     * 如果当前存在事务，加入该事务；否则创建新事务
     */
    REQUIRED,

    /**
     * 始终创建新事务，如果当前存在事务，则暂停当前事务
     */
    REQUIRES_NEW,

    /**
     * 如果当前存在事务，使用嵌套事务（Savepoint）；否则创建新事务
     */
    NESTED,

    /**
     * 不使用事务，如果当前存在事务，则抛出异常
     */
    NEVER,

    /**
     * 不使用事务，如果当前存在事务，则暂停
     */
    NOT_SUPPORTED,

    /**
     * 必须在事务内执行，否则抛出异常
     */
    MANDATORY,

    /**
     * 支持当前事务，如果不存在则不使用事务
     */
    SUPPORTS;
}
