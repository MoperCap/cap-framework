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
import java.util.Map;

/**
 * 事务切面 - 处理所有 {@link Transactional} 方法的事务逻辑
 *
 * <p>与 cap-aop 模块集成，使用 {@link Around} 切面拦截 {@link Transactional} 方法。
 *
 * <p>职责：
 * <ol>
 *   <li>拦截所有标注 {@link Transactional} 的方法或类下的所有方法</li>
 *   <li>在方法执行前根据传播性开启事务</li>
 *   <li>在方法成功后提交事务</li>
 *   <li>在方法异常后按回滚规则回滚事务</li>
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

    private Object handleTransaction(ProceedingJoinPoint joinPoint, Transactional tx,
                                     TransactionManager txManager) throws Throwable {
        Method method = joinPoint.getMethod();
        log.debug("处理事务方法: method={}", method.getName());

        // Capture the transaction context before we potentially push a new entry.
        // We are only responsible for commit/rollback if we started a new transaction.
        TransactionContext.TransactionInfo existingTx = TransactionContext.getCurrentTransaction();

        handlePropagation(tx.propagation(), tx.readOnly(), tx.isolation(), txManager);

        TransactionContext.TransactionInfo txInfo = TransactionContext.getCurrentTransaction();
        // We own the transaction only when a new entry was pushed onto the stack
        boolean isOwner = txInfo != null && txInfo != existingTx;

        try {
            Object result = joinPoint.proceed();

            if (isOwner) {
                checkTimeout(tx, txInfo);
                log.debug("事务提交: method={}", method.getName());
                txManager.commit(txInfo.getConnection());
            }

            return result;
        } catch (Throwable throwable) {
            if (isOwner) {
                if (shouldRollback(tx, throwable)) {
                    log.debug("事务回滚: method={}, exception={}",
                            method.getName(), throwable.getClass().getName());
                    try {
                        txManager.rollback(txInfo.getConnection());
                    } catch (Exception rollbackEx) {
                        log.error("事务回滚失败", rollbackEx);
                    }
                } else {
                    txManager.commit(txInfo.getConnection());
                }
            }
            throw throwable;
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
     * Handles transaction propagation by starting/joining a transaction as required.
     *
     * <p>A new transaction entry is pushed onto the context stack only when this method
     * actually begins a transaction. The caller tracks whether a new entry was pushed
     * by comparing {@link TransactionContext#getCurrentTransaction()} before and after
     * this call, and uses that to decide whether it owns the commit/rollback.
     */
    private void handlePropagation(Propagation propagation, boolean readOnly,
                                   IsolationLevel isolation, TransactionManager txManager)
            throws Exception {
        TransactionContext.TransactionInfo currentTx = TransactionContext.getCurrentTransaction();

        switch (propagation) {
            case REQUIRED:
                if (currentTx == null) {
                    txManager.beginTransaction(readOnly, isolation);
                    log.debug("REQUIRED: 创建新事务");
                } else {
                    log.debug("REQUIRED: 加入现有事务");
                }
                break;

            case REQUIRES_NEW:
                txManager.beginTransaction(readOnly, isolation);
                log.debug("REQUIRES_NEW: 创建新事务");
                break;

            case NESTED:
                if (currentTx == null) {
                    txManager.beginTransaction(readOnly, isolation);
                    log.debug("NESTED: 创建新事务");
                } else {
                    log.debug("NESTED: 在现有事务中执行");
                }
                break;

            case NEVER:
                if (currentTx != null) {
                    throw new TransactionException("NEVER propagation but transaction already exists");
                }
                log.debug("NEVER: 不使用事务");
                break;

            case NOT_SUPPORTED:
                if (currentTx != null) {
                    log.debug("NOT_SUPPORTED: 暂停当前事务");
                }
                break;

            case MANDATORY:
                if (currentTx == null) {
                    throw new TransactionException("MANDATORY propagation but no transaction exists");
                }
                log.debug("MANDATORY: 在事务中执行");
                break;

            case SUPPORTS:
                if (currentTx != null) {
                    log.debug("SUPPORTS: 加入现有事务");
                } else {
                    log.debug("SUPPORTS: 不在事务中");
                }
                break;

            default:
                break;
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
