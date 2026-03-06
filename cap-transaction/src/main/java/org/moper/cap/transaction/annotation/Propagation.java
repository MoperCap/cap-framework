package org.moper.cap.transaction.annotation;

/**
 * 事务传播性，定义在已有事务上下文中应如何开始一个新的事务方法。
 */
public enum Propagation {

    /**
     * 如果当前存在事务，则加入该事务；否则创建一个新事务。
     * 这是最常用的默认传播性。
     */
    REQUIRED,

    /**
     * 如果当前存在事务，则加入该事务；否则以无事务方式执行。
     */
    SUPPORTS,

    /**
     * 如果当前存在事务，则加入该事务；否则抛出异常。
     */
    MANDATORY,

    /**
     * 总是创建一个新事务；如果当前存在事务，则将其挂起。
     */
    REQUIRES_NEW,

    /**
     * 总是以无事务方式执行；如果当前存在事务，则将其挂起。
     */
    NOT_SUPPORTED,

    /**
     * 以无事务方式执行；如果当前存在事务，则抛出异常。
     */
    NEVER,

    /**
     * 如果当前存在事务，则在嵌套事务中执行（使用 Savepoint）；
     * 否则与 {@link #REQUIRED} 行为相同，创建一个新事务。
     */
    NESTED
}
