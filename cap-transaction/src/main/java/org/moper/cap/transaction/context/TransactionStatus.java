package org.moper.cap.transaction.context;

import lombok.Getter;

import java.sql.Connection;
import java.sql.Savepoint;

/**
 * 事务状态信息
 *
 * <p>表示当前激活事务的状态，由 {@link TransactionContext} 管理，
 * 通过 {@link org.moper.cap.transaction.template.TransactionCallback} 暴露给编程式事务的回调方法。
 *
 * <p>主要功能：
 * <ul>
 *   <li>查询事务是否为新建事务</li>
 *   <li>查询是否存在 Savepoint（NESTED 传播性）</li>
 *   <li>将事务标记为仅回滚（{@link #setRollbackOnly()}）</li>
 *   <li>超时检测（{@link #getStartTime()}）</li>
 * </ul>
 */
@Getter
public class TransactionStatus {

    /**
     * 事务连接，若在非事务上下文（NEVER / NOT_SUPPORTED）中执行则为 {@code null}。
     */
    private final Connection connection;

    /**
     * 是否为新建事务（{@code true} 表示此次调用开启了一个全新事务）。
     */
    private final boolean newTransaction;

    /**
     * NESTED 传播性中创建的 Savepoint，无 Savepoint 时为 {@code null}。
     */
    private final Savepoint savepoint;

    /**
     * 事务开始时间（毫秒）。
     */
    private final long startTime;

    /**
     * 仅回滚标记：设置后事务将强制回滚而非提交。
     */
    private volatile boolean rollbackOnly;

    /**
     * 完成标记：提交或回滚后置为 {@code true}。
     */
    private volatile boolean completed;

    /**
     * 创建不含 Savepoint 的事务状态。
     */
    public TransactionStatus(Connection connection, boolean newTransaction) {
        this(connection, newTransaction, null);
    }

    /**
     * 创建含 Savepoint 的事务状态（NESTED 传播性）。
     */
    public TransactionStatus(Connection connection, boolean newTransaction, Savepoint savepoint) {
        this.connection = connection;
        this.newTransaction = newTransaction;
        this.savepoint = savepoint;
        this.startTime = System.currentTimeMillis();
    }

    /**
     * 是否存在 Savepoint。
     */
    public boolean hasSavepoint() {
        return savepoint != null;
    }

    /**
     * 是否已标记为仅回滚。
     */
    public boolean isRollbackOnly() {
        return rollbackOnly;
    }

    /**
     * 将当前事务标记为仅回滚。
     *
     * <p>调用此方法后，{@link org.moper.cap.transaction.template.TransactionTemplate}
     * 在回调执行完毕后将回滚而非提交事务。
     */
    public void setRollbackOnly() {
        this.rollbackOnly = true;
    }

    /**
     * 事务是否已完成（已提交或已回滚）。
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * 将事务标记为已完成，由框架在提交或回滚后调用。
     */
    public void setCompleted() {
        this.completed = true;
    }
}
