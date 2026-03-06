package org.moper.cap.transaction.aspect;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.transaction.annotation.IsolationLevel;
import org.moper.cap.transaction.annotation.Propagation;
import org.moper.cap.transaction.annotation.Transactional;
import org.moper.cap.transaction.context.TransactionContext;
import org.moper.cap.transaction.exception.TransactionException;
import org.moper.cap.transaction.manager.TransactionManager;

import java.lang.reflect.Method;
import java.sql.Connection;

/**
 * 事务切面 - 处理 @Transactional 的核心逻辑
 *
 * <p>工作流程：
 * <ol>
 *   <li>{@link #handleTransactionBegin} - 在方法执行前根据传播性决定是否开启事务</li>
 *   <li>方法执行</li>
 *   <li>{@link #handleTransactionEnd} - 在方法执行后提交或回滚事务</li>
 * </ol>
 */
@Slf4j
public class TransactionAspect {

    private final TransactionManager transactionManager;

    public TransactionAspect(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * 处理事务开始。
     *
     * <p>根据方法上的 {@link Transactional} 注解配置以及传播性语义决定是否开启新事务。
     *
     * @param method 带有 {@link Transactional} 注解的目标方法
     * @throws Exception 开启事务失败时抛出
     */
    public void handleTransactionBegin(Method method) throws Exception {
        Transactional tx = method.getAnnotation(Transactional.class);
        if (tx == null) {
            return;
        }

        log.debug("处理事务开始: method={}", method.getName());
        handlePropagation(tx.propagation(), tx.readOnly(), tx.isolation());
    }

    /**
     * 处理事务结束（提交或回滚）。
     *
     * <p>若 {@code throwable} 不为 null，根据回滚规则决定是否回滚；否则提交事务。
     *
     * @param method    带有 {@link Transactional} 注解的目标方法
     * @param throwable 方法执行期间抛出的异常或错误，正常完成时为 null
     * @throws Exception 提交或回滚失败时抛出
     */
    public void handleTransactionEnd(Method method, Throwable throwable) throws Exception {
        Transactional tx = method.getAnnotation(Transactional.class);
        if (tx == null) {
            return;
        }

        TransactionContext.TransactionInfo txInfo = TransactionContext.getCurrentTransaction();
        if (txInfo == null) {
            return;
        }

        // Check timeout before commit
        checkTimeout(tx, txInfo);

        Connection connection = txInfo.getConnection();

        try {
            if (throwable != null && shouldRollback(tx, throwable)) {
                log.debug("事务回滚: method={}, exception={}", method.getName(), throwable.getClass().getName());
                transactionManager.rollback(connection);
            } else {
                log.debug("事务提交: method={}", method.getName());
                transactionManager.commit(connection);
            }
        } catch (Exception e) {
            log.error("处理事务结束失败", e);
            throw e;
        }
    }

    /**
     * 根据传播性处理事务。
     */
    private void handlePropagation(Propagation propagation, boolean readOnly, IsolationLevel isolation) throws Exception {
        TransactionContext.TransactionInfo currentTx = TransactionContext.getCurrentTransaction();

        switch (propagation) {
            case REQUIRED:
                if (currentTx == null) {
                    transactionManager.beginTransaction(readOnly, isolation);
                    log.debug("REQUIRED: 创建新事务");
                } else {
                    log.debug("REQUIRED: 加入现有事务");
                }
                break;

            case REQUIRES_NEW:
                if (currentTx != null) {
                    log.debug("REQUIRES_NEW: 暂停当前事务，创建新事务");
                }
                transactionManager.beginTransaction(readOnly, isolation);
                break;

            case NESTED:
                if (currentTx == null) {
                    transactionManager.beginTransaction(readOnly, isolation);
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
        }
    }

    /**
     * 检查事务是否超时。
     *
     * <p>仅当 {@link Transactional#timeout()} &gt; 0 时有效，在方法执行完成、提交之前检查已用时间。
     * 若超时则抛出 {@link TransactionException} 触发回滚。
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
     * 根据 {@link Transactional} 配置判断给定异常/错误是否应触发回滚。
     *
     * <p>规则（按优先级）：
     * <ol>
     *   <li>{@link Error} 及其子类：始终回滚</li>
     *   <li>{@code noRollbackFor} 匹配 → 不回滚</li>
     *   <li>{@code rollbackFor} 非空且匹配 → 回滚</li>
     *   <li>{@code rollbackFor} 非空但不匹配 → 不回滚</li>
     *   <li>默认：{@link RuntimeException} 及其子类回滚</li>
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
