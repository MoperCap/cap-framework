package org.moper.cap.transaction.aspect;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.aop.annotation.Around;
import org.moper.cap.aop.annotation.Aspect;
import org.moper.cap.aop.model.JoinPoint;
import org.moper.cap.aop.model.ProceedingJoinPoint;
import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.transaction.annotation.IsolationLevel;
import org.moper.cap.transaction.annotation.Propagation;
import org.moper.cap.transaction.annotation.Transactional;
import org.moper.cap.transaction.context.TransactionContext;
import org.moper.cap.transaction.exception.TransactionException;
import org.moper.cap.transaction.manager.TransactionManager;

import java.lang.reflect.Method;
import java.sql.Savepoint;
import java.util.Map;

/**
 * 事务切面 - 处理所有 {@link Transactional} 方法的事务逻辑
 *
 * <p>与 cap-aop 模块集成，使用 {@link Around} 切面拦截 {@link Transactional} 方法。
 *
 * <p>支持的传播性：
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
 * <p>职责：
 * <ol>
 *   <li>拦截所有标注 {@link Transactional} 的方法或类下的所有方法</li>
 *   <li>在方法执行前根据传播性开启/加入/挂起事务</li>
 *   <li>在方法成功后提交事务或释放 Savepoint</li>
 *   <li>在方法异常后按回滚规则回滚事务或回滚到 Savepoint</li>
 *   <li>在方法完成后恢复被挂起的事务</li>
 * </ol>
 */
@Slf4j
@Aspect
public class TransactionAspect {

    private final BeanContainer beanContainer;

    /**
     * Cached {@link TransactionManager} – looked up lazily on first use so that the
     * aspect can be registered during bootstrap before the manager bean is available.
     */
    private volatile TransactionManager cachedTransactionManager;

    public TransactionAspect(BeanContainer beanContainer) {
        this.beanContainer = beanContainer;
    }

    /**
     * Around advice that intercepts every method annotated with {@link Transactional}
     * (either on the method itself or on its declaring class).
     */
    @Around("@target(org.moper.cap.transaction.annotation.Transactional) || @method(org.moper.cap.transaction.annotation.Transactional)")
    public Object transactionInterceptor(ProceedingJoinPoint joinPoint) throws Throwable {
        Transactional tx = getTransactionalAnnotation(joinPoint);
        if (tx == null) {
            return joinPoint.proceed();
        }

        TransactionManager txManager = getTransactionManager();
        if (txManager == null) {
            log.warn("⚠️  未找到 TransactionManager 实现，方法 [{}] 将在无事务环境下执行",
                    joinPoint.getMethod().getName());
            return joinPoint.proceed();
        }

        return handleTransaction(joinPoint, tx, txManager);
    }

    // -------------------------------------------------------------------------
    // Transaction lifecycle
    // -------------------------------------------------------------------------

    /**
     * Internal value type that carries the outcome of {@link #handlePropagation}.
     *
     * @param isOwner      {@code true} when this invocation started a brand-new top-level
     *                     transaction that it must commit or roll back on completion.
     * @param suspendedTx  The transaction that was suspended (for REQUIRES_NEW /
     *                     NOT_SUPPORTED); {@code null} if nothing was suspended.
     * @param savepoint    The Savepoint created for a NESTED transaction inside an
     *                     existing transaction; {@code null} otherwise.
     */
    private record PropagationContext(boolean isOwner,
                                      TransactionContext.TransactionInfo suspendedTx,
                                      Savepoint savepoint) {}

    private Object handleTransaction(ProceedingJoinPoint joinPoint, Transactional tx,
                                     TransactionManager txManager) throws Throwable {
        Method method = joinPoint.getMethod();
        log.debug("处理事务方法: method={}, propagation={}", method.getName(), tx.propagation());

        PropagationContext propCtx = handlePropagation(tx.propagation(), tx.readOnly(),
                tx.isolation(), txManager);
        TransactionContext.TransactionInfo txInfo = TransactionContext.getCurrentTransaction();

        try {
            Object result = joinPoint.proceed();

            if (propCtx.isOwner() && txInfo != null) {
                checkTimeout(tx, txInfo);
                log.debug("事务提交: method={}", method.getName());
                txManager.commit(txInfo.getConnection());
            } else if (propCtx.savepoint() != null && txInfo != null) {
                // NESTED success – try to release the savepoint
                try {
                    txManager.releaseSavepoint(txInfo.getConnection(), propCtx.savepoint());
                } catch (Exception e) {
                    log.debug("释放 Savepoint 失败（已忽略）: {}", e.getMessage());
                }
            }

            return result;
        } catch (Throwable throwable) {
            if (propCtx.isOwner() && txInfo != null) {
                if (shouldRollback(tx, throwable)) {
                    log.debug("事务回滚: method={}, exception={}",
                            method.getName(), throwable.getClass().getName());
                    try {
                        txManager.rollback(txInfo.getConnection());
                    } catch (Exception rollbackEx) {
                        log.error("事务回滚失败", rollbackEx);
                    }
                } else {
                    try {
                        txManager.commit(txInfo.getConnection());
                    } catch (Exception commitEx) {
                        log.error("事务提交失败", commitEx);
                    }
                }
            } else if (propCtx.savepoint() != null && txInfo != null) {
                // NESTED failure – roll back to or release the savepoint
                if (shouldRollback(tx, throwable)) {
                    log.debug("NESTED 回滚到 Savepoint: method={}, exception={}",
                            method.getName(), throwable.getClass().getName());
                    try {
                        txManager.rollbackToSavepoint(txInfo.getConnection(), propCtx.savepoint());
                    } catch (Exception rollbackEx) {
                        log.error("回滚到 Savepoint 失败", rollbackEx);
                    }
                } else {
                    try {
                        txManager.releaseSavepoint(txInfo.getConnection(), propCtx.savepoint());
                    } catch (Exception releaseEx) {
                        log.debug("释放 Savepoint 失败（已忽略）: {}", releaseEx.getMessage());
                    }
                }
            }
            throw throwable;
        } finally {
            // Always restore a suspended transaction, regardless of outcome.
            if (propCtx.suspendedTx() != null) {
                TransactionContext.resumeTransaction(propCtx.suspendedTx());
                log.debug("恢复挂起的事务: method={}", method.getName());
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the effective {@link Transactional} annotation for the join point.
     * Method-level annotation takes precedence over class-level.
     */
    private Transactional getTransactionalAnnotation(JoinPoint joinPoint) {
        Method method = joinPoint.getMethod();
        Transactional methodTx = method.getAnnotation(Transactional.class);
        if (methodTx != null) {
            return methodTx;
        }
        // Fall back to class-level annotation on the actual target class
        return joinPoint.getTarget().getClass().getAnnotation(Transactional.class);
    }

    /** Lazily resolves the {@link TransactionManager} from the bean container. */
    private TransactionManager getTransactionManager() {
        if (cachedTransactionManager == null) {
            synchronized (this) {
                if (cachedTransactionManager == null) {
                    try {
                        Map<String, TransactionManager> managers =
                                beanContainer.getBeansOfType(TransactionManager.class);
                        if (!managers.isEmpty()) {
                            cachedTransactionManager = managers.values().iterator().next();
                        }
                    } catch (Exception e) {
                        log.debug("TransactionManager 查找失败: {}", e.getMessage());
                    }
                }
            }
        }
        return cachedTransactionManager;
    }

    /**
     * Applies the requested propagation behaviour and returns a {@link PropagationContext}
     * that describes what happened (new transaction owner, suspended transaction, or savepoint).
     *
     * <ul>
     *   <li><b>REQUIRED</b>  – join existing or begin new</li>
     *   <li><b>REQUIRES_NEW</b> – suspend existing (if any), begin new</li>
     *   <li><b>NESTED</b>    – if existing: create Savepoint; else begin new</li>
     *   <li><b>NEVER</b>     – fail if existing transaction present</li>
     *   <li><b>NOT_SUPPORTED</b> – suspend existing (if any), proceed non-transactionally</li>
     *   <li><b>MANDATORY</b> – fail if no existing transaction</li>
     *   <li><b>SUPPORTS</b>  – join if existing, else proceed non-transactionally</li>
     * </ul>
     */
    private PropagationContext handlePropagation(Propagation propagation, boolean readOnly,
                                                 IsolationLevel isolation,
                                                 TransactionManager txManager) throws Exception {
        TransactionContext.TransactionInfo currentTx = TransactionContext.getCurrentTransaction();

        switch (propagation) {
            case REQUIRED:
                if (currentTx == null) {
                    txManager.beginTransaction(readOnly, isolation);
                    log.debug("REQUIRED: 创建新事务");
                    return new PropagationContext(true, null, null);
                }
                log.debug("REQUIRED: 加入现有事务 (depth={})", TransactionContext.getTransactionDepth());
                return new PropagationContext(false, null, null);

            case REQUIRES_NEW: {
                TransactionContext.TransactionInfo suspended = TransactionContext.suspendTransaction();
                if (suspended != null) {
                    log.debug("REQUIRES_NEW: 挂起现有事务，创建独立新事务");
                } else {
                    log.debug("REQUIRES_NEW: 创建新事务");
                }
                txManager.beginTransaction(readOnly, isolation);
                return new PropagationContext(true, suspended, null);
            }

            case NESTED:
                if (currentTx == null) {
                    txManager.beginTransaction(readOnly, isolation);
                    log.debug("NESTED: 无外层事务，创建新事务");
                    return new PropagationContext(true, null, null);
                }
                // Existing transaction – create a savepoint for partial rollback
                Savepoint sp = txManager.createSavepoint(currentTx.getConnection());
                log.debug("NESTED: 在现有事务中创建 Savepoint");
                return new PropagationContext(false, null, sp);

            case NEVER:
                if (currentTx != null) {
                    throw new TransactionException(
                            "NEVER propagation: a transaction is already active");
                }
                log.debug("NEVER: 以非事务方式执行");
                return new PropagationContext(false, null, null);

            case NOT_SUPPORTED: {
                TransactionContext.TransactionInfo suspended = TransactionContext.suspendTransaction();
                if (suspended != null) {
                    log.debug("NOT_SUPPORTED: 挂起现有事务，以非事务方式执行");
                } else {
                    log.debug("NOT_SUPPORTED: 以非事务方式执行");
                }
                return new PropagationContext(false, suspended, null);
            }

            case MANDATORY:
                if (currentTx == null) {
                    throw new TransactionException(
                            "MANDATORY propagation: no existing transaction found");
                }
                log.debug("MANDATORY: 在事务中执行 (depth={})", TransactionContext.getTransactionDepth());
                return new PropagationContext(false, null, null);

            case SUPPORTS:
                if (currentTx != null) {
                    log.debug("SUPPORTS: 加入现有事务 (depth={})", TransactionContext.getTransactionDepth());
                } else {
                    log.debug("SUPPORTS: 以非事务方式执行");
                }
                return new PropagationContext(false, null, null);

            default:
                return new PropagationContext(false, null, null);
        }
    }

    /**
     * Checks whether the transaction has exceeded its configured timeout.
     */
    private void checkTimeout(Transactional tx, TransactionContext.TransactionInfo txInfo) {
        int timeout = tx.timeout();
        if (timeout <= 0) return;

        long elapsedSeconds = (System.currentTimeMillis() - txInfo.getStartTime()) / 1000;
        if (elapsedSeconds >= timeout) {
            throw new TransactionException(
                    "Transaction timed out after " + elapsedSeconds + "s (limit: " + timeout + "s)");
        }
    }

    /**
     * Determines whether the given throwable should trigger a rollback.
     *
     * <p>Rules (in priority order):
     * <ol>
     *   <li>{@link Error} subclasses always roll back</li>
     *   <li>{@code noRollbackFor} match → no rollback</li>
     *   <li>{@code rollbackFor} non-empty and matches → rollback</li>
     *   <li>{@code rollbackFor} non-empty but no match → no rollback</li>
     *   <li>Default: {@link RuntimeException} subclasses roll back</li>
     * </ol>
     */
    boolean shouldRollback(Transactional tx, Throwable throwable) {
        if (!(throwable instanceof Exception exception)) {
            // Errors always roll back
            return true;
        }

        for (Class<? extends Exception> noRollback : tx.noRollbackFor()) {
            if (noRollback.isInstance(exception)) {
                return false;
            }
        }

        if (tx.rollbackFor().length > 0) {
            for (Class<? extends Exception> rollback : tx.rollbackFor()) {
                if (rollback.isInstance(exception)) {
                    return true;
                }
            }
            return false;
        }

        return throwable instanceof RuntimeException;
    }
}
