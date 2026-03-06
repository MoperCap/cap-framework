package org.moper.cap.transaction.template;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.transaction.annotation.IsolationLevel;
import org.moper.cap.transaction.annotation.Propagation;
import org.moper.cap.transaction.context.TransactionContext;
import org.moper.cap.transaction.context.TransactionStatus;
import org.moper.cap.transaction.exception.TransactionException;
import org.moper.cap.transaction.manager.TransactionManager;

import java.sql.Connection;
import java.sql.Savepoint;

/**
 * 事务模板 - 提供编程式事务 API
 *
 * <p>允许在任何地方使用事务，无需 {@code @Transactional} 注解。
 *
 * <p>支持所有传播性：
 * <ul>
 *   <li>{@link Propagation#REQUIRED}      – 加入已有事务或创建新事务（默认）</li>
 *   <li>{@link Propagation#REQUIRES_NEW}  – 始终创建新事务，挂起已有事务</li>
 *   <li>{@link Propagation#NESTED}        – 嵌套事务（Savepoint 支持）</li>
 *   <li>{@link Propagation#SUPPORTS}      – 支持但非强制事务</li>
 *   <li>{@link Propagation#MANDATORY}     – 强制在事务中执行</li>
 *   <li>{@link Propagation#NEVER}         – 禁止事务</li>
 *   <li>{@link Propagation#NOT_SUPPORTED} – 暂停事务后以非事务方式执行</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * @Inject
 * private TransactionTemplate txTemplate;
 *
 * public void complexOperation() {
 *     txTemplate.execute(() -> {
 *         userService.createUser(...);
 *         orderService.createOrder(...);
 *         return null;
 *     });
 * }
 * }</pre>
 *
 * <p>链式调用示例：
 * <pre>{@code
 * txTemplate
 *     .isolationLevel(IsolationLevel.REPEATABLE_READ)
 *     .propagation(Propagation.REQUIRES_NEW)
 *     .timeout(30)
 *     .execute(() -> {
 *         return result;
 *     });
 * }</pre>
 */
@Slf4j
public class TransactionTemplate {

    private final TransactionManager transactionManager;
    private boolean readOnly = false;
    private IsolationLevel isolationLevel = IsolationLevel.READ_COMMITTED;
    private Propagation propagation = Propagation.REQUIRED;
    private int timeout = -1;

    public TransactionTemplate(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * 在配置的事务语义下执行给定的业务逻辑。
     *
     * <p>根据 {@link #propagation} 配置选择以下行为之一：
     * <ul>
     *   <li>REQUIRED      – 加入已有事务，或开启并在完成后提交/回滚新事务</li>
     *   <li>REQUIRES_NEW  – 挂起已有事务，开启并在完成后提交/回滚新事务，然后恢复被挂起的事务</li>
     *   <li>NESTED        – 若已有事务则创建 Savepoint；失败回滚到 Savepoint，成功则释放</li>
     *   <li>SUPPORTS      – 若已有事务则加入；否则以非事务方式执行</li>
     *   <li>MANDATORY     – 必须已有事务，否则抛出异常</li>
     *   <li>NEVER         – 不允许事务，否则抛出异常</li>
     *   <li>NOT_SUPPORTED – 挂起已有事务，以非事务方式执行，然后恢复</li>
     * </ul>
     *
     * @param action 事务内的业务逻辑回调
     * @param <T>    返回值类型
     * @return 业务逻辑的返回值
     * @throws RuntimeException 业务逻辑抛出异常时封装后重新抛出
     */
    public <T> T execute(TransactionCallback<T> action) {
        log.debug("编程式事务执行: propagation={}, isolationLevel={}", propagation, isolationLevel);

        TransactionContext.TransactionInfo currentTx = TransactionContext.getCurrentTransaction();

        switch (propagation) {
            case REQUIRED:
                return executeRequired(action, currentTx);

            case REQUIRES_NEW:
                return executeRequiresNew(action);

            case NESTED:
                return executeNested(action, currentTx);

            case SUPPORTS: {
                // Join existing or run non-transactionally
                TransactionStatus status = (currentTx != null)
                        ? new TransactionStatus(currentTx.getConnection(), false)
                        : new TransactionStatus(null, false);
                if (currentTx != null) {
                    log.debug("SUPPORTS: 加入现有事务");
                } else {
                    log.debug("SUPPORTS: 以非事务方式执行");
                }
                return doInvoke(action, status);
            }

            case MANDATORY: {
                if (currentTx == null) {
                    throw new TransactionException(
                            "MANDATORY propagation: no existing transaction found");
                }
                log.debug("MANDATORY: 在事务中执行");
                TransactionStatus status = new TransactionStatus(currentTx.getConnection(), false);
                return doInvoke(action, status);
            }

            case NEVER: {
                if (currentTx != null) {
                    throw new TransactionException(
                            "NEVER propagation: a transaction is already active");
                }
                log.debug("NEVER: 以非事务方式执行");
                TransactionStatus status = new TransactionStatus(null, false);
                return doInvoke(action, status);
            }

            case NOT_SUPPORTED:
                return executeNotSupported(action);

            default:
                return executeRequired(action, currentTx);
        }
    }

    // -------------------------------------------------------------------------
    // Per-propagation execution strategies
    // -------------------------------------------------------------------------

    /** REQUIRED – join existing or begin new. */
    private <T> T executeRequired(TransactionCallback<T> action,
                                  TransactionContext.TransactionInfo currentTx) {
        if (currentTx != null) {
            log.debug("REQUIRED: 加入现有事务 (depth={})", TransactionContext.getTransactionDepth());
            TransactionStatus status = new TransactionStatus(currentTx.getConnection(), false);
            return doInvoke(action, status);
        }
        return beginAndExecute(action);
    }

    /** REQUIRES_NEW – suspend existing (if any), begin new, restore on completion. */
    private <T> T executeRequiresNew(TransactionCallback<T> action) {
        TransactionContext.TransactionInfo suspended = TransactionContext.suspendTransaction();
        if (suspended != null) {
            log.debug("REQUIRES_NEW: 挂起现有事务，创建独立新事务");
        } else {
            log.debug("REQUIRES_NEW: 创建新事务");
        }
        try {
            return beginAndExecute(action);
        } finally {
            TransactionContext.resumeTransaction(suspended);
            if (suspended != null) {
                log.debug("REQUIRES_NEW: 已恢复挂起事务");
            }
        }
    }

    /** NESTED – savepoint if inside existing tx; otherwise begin new. */
    private <T> T executeNested(TransactionCallback<T> action,
                                TransactionContext.TransactionInfo currentTx) {
        if (currentTx == null) {
            log.debug("NESTED: 无外层事务，创建新事务");
            return beginAndExecute(action);
        }

        // Create a savepoint on the current connection
        Connection conn = currentTx.getConnection();
        Savepoint savepoint;
        try {
            savepoint = transactionManager.createSavepoint(conn);
            log.debug("NESTED: 在现有事务中创建 Savepoint");
        } catch (Exception e) {
            throw new RuntimeException("Failed to create savepoint for NESTED transaction", e);
        }

        TransactionStatus status = new TransactionStatus(conn, false, savepoint);
        try {
            T result = doInvoke(action, status);
            // Success – release the savepoint
            try {
                transactionManager.releaseSavepoint(conn, savepoint);
            } catch (Exception e) {
                log.debug("释放 Savepoint 失败（已忽略）: {}", e.getMessage());
            }
            return result;
        } catch (Exception e) {
            // Failure – roll back to savepoint
            try {
                transactionManager.rollbackToSavepoint(conn, savepoint);
                log.debug("NESTED: 已回滚到 Savepoint");
            } catch (Exception rollbackEx) {
                log.error("回滚到 Savepoint 失败", rollbackEx);
            }
            throw new RuntimeException("NESTED transaction execution failed", e);
        }
    }

    /** NOT_SUPPORTED – suspend existing (if any), execute non-transactionally, restore. */
    private <T> T executeNotSupported(TransactionCallback<T> action) {
        TransactionContext.TransactionInfo suspended = TransactionContext.suspendTransaction();
        if (suspended != null) {
            log.debug("NOT_SUPPORTED: 挂起现有事务，以非事务方式执行");
        } else {
            log.debug("NOT_SUPPORTED: 以非事务方式执行");
        }
        try {
            TransactionStatus status = new TransactionStatus(null, false);
            return doInvoke(action, status);
        } finally {
            TransactionContext.resumeTransaction(suspended);
            if (suspended != null) {
                log.debug("NOT_SUPPORTED: 已恢复挂起事务");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Begins a new transaction, executes the action, and commits/rolls back.
     * Also enforces the configured {@link #timeout}.
     */
    private <T> T beginAndExecute(TransactionCallback<T> action) {
        Connection conn = null;
        try {
            conn = transactionManager.beginTransaction(readOnly, isolationLevel);
            log.debug("编程式事务已开始");

            TransactionStatus status = new TransactionStatus(conn, true);
            T result = doInvoke(action, status);

            checkTimeout(status.getStartTime());

            if (status.isRollbackOnly()) {
                transactionManager.rollback(conn);
                log.debug("编程式事务已回滚（rollback-only 标记）");
            } else {
                transactionManager.commit(conn);
                log.debug("编程式事务已提交");
            }
            status.setCompleted();
            return result;
        } catch (Exception e) {
            if (conn != null) {
                try {
                    transactionManager.rollback(conn);
                    log.debug("编程式事务已回滚");
                } catch (Exception rollbackEx) {
                    log.error("回滚失败", rollbackEx);
                }
            }
            if (e instanceof RuntimeException rte) {
                throw rte;
            }
            throw new RuntimeException("Transaction execution failed", e);
        }
    }

    /** Invokes the callback with the given {@link TransactionStatus}, wrapping checked exceptions. */
    private <T> T doInvoke(TransactionCallback<T> action, TransactionStatus status) {
        try {
            return action.doInTransaction(status);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Transaction callback threw checked exception", e);
        }
    }

    /** Checks whether the timeout (if configured) has been exceeded.
     *  Both -1 (default) and 0 are treated as "no timeout", consistent with
     *  {@code @Transactional#timeout} semantics in {@code TransactionAspect}.
     */
    private void checkTimeout(long startTime) {
        if (timeout <= 0) return;
        long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
        if (elapsedSeconds >= timeout) {
            throw new TransactionException(
                    "Transaction timed out after " + elapsedSeconds + "s (limit: " + timeout + "s)");
        }
    }

    // -------------------------------------------------------------------------
    // Fluent builder methods (return copies to support thread-safe chaining)
    // -------------------------------------------------------------------------

    /**
     * 设置只读模式（返回新实例以支持线程安全的链式调用）。
     */
    public TransactionTemplate readOnly() {
        TransactionTemplate copy = copy();
        copy.readOnly = true;
        return copy;
    }

    /**
     * 设置隔离级别（返回新实例以支持线程安全的链式调用）。
     */
    public TransactionTemplate isolationLevel(IsolationLevel level) {
        TransactionTemplate copy = copy();
        copy.isolationLevel = level;
        return copy;
    }

    /**
     * 设置传播性（返回新实例以支持线程安全的链式调用）。
     */
    public TransactionTemplate propagation(Propagation prop) {
        TransactionTemplate copy = copy();
        copy.propagation = prop;
        return copy;
    }

    /**
     * 设置超时时间（秒）（返回新实例以支持线程安全的链式调用）。
     */
    public TransactionTemplate timeout(int seconds) {
        TransactionTemplate copy = copy();
        copy.timeout = seconds;
        return copy;
    }

    private TransactionTemplate copy() {
        TransactionTemplate copy = new TransactionTemplate(this.transactionManager);
        copy.readOnly = this.readOnly;
        copy.isolationLevel = this.isolationLevel;
        copy.propagation = this.propagation;
        copy.timeout = this.timeout;
        return copy;
    }
}
