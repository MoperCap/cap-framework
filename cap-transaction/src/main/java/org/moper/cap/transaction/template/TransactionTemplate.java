package org.moper.cap.transaction.template;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.transaction.annotation.IsolationLevel;
import org.moper.cap.transaction.context.TransactionContext;
import org.moper.cap.transaction.context.TransactionStatus;
import org.moper.cap.transaction.manager.TransactionManager;

import java.sql.Connection;

/**
 * 编程式事务模板，简化手动事务管理的样板代码。
 *
 * <p>通过 {@link #execute(TransactionCallback)} 方法，将业务回调包裹在事务中执行：
 * <ol>
 *   <li>自动开启事务</li>
 *   <li>执行 {@link TransactionCallback#doInTransaction(TransactionStatus)}</li>
 *   <li>若回调正常返回且未标记 rollbackOnly，则提交事务</li>
 *   <li>若回调抛出异常或标记 rollbackOnly，则回滚事务</li>
 * </ol>
 *
 * <p>通常由 {@code cap-data} 模块的 {@code TransactionManagerFactory} 以 Bean 方式提供，
 * 可直接通过 {@link org.moper.cap.bean.annotation.Inject} 注入使用。
 */
@Slf4j
public class TransactionTemplate {

    private final TransactionManager transactionManager;

    public TransactionTemplate(TransactionManager transactionManager) {
        if (transactionManager == null) {
            throw new IllegalArgumentException("TransactionManager must not be null");
        }
        this.transactionManager = transactionManager;
    }

    /**
     * 在事务中执行给定的回调，并返回回调的结果。
     *
     * <p>事务属性：默认隔离级别（{@link IsolationLevel#DEFAULT}）、非只读、无超时。
     *
     * @param callback 业务回调
     * @param <T>      返回类型
     * @return 回调返回值
     * @throws RuntimeException 若事务执行失败
     */
    public <T> T execute(TransactionCallback<T> callback) {
        Connection connection = null;
        TransactionStatus status = new TransactionStatus();
        long startTime = System.currentTimeMillis();

        try {
            connection = transactionManager.beginTransaction(false, IsolationLevel.DEFAULT);
            log.debug("编程式事务已开启");

            T result = callback.doInTransaction(status);

            TransactionContext.TransactionInfo txInfo = TransactionContext.getCurrentTransaction();
            boolean rollbackOnly = status.isRollbackOnly()
                    || (txInfo != null && txInfo.isRollbackOnly());

            if (rollbackOnly) {
                log.debug("事务标记为 rollbackOnly，执行回滚");
                transactionManager.rollback(connection);
            } else {
                log.debug("编程式事务提交，耗时 {}ms", System.currentTimeMillis() - startTime);
                transactionManager.commit(connection);
            }

            return result;

        } catch (Exception e) {
            log.debug("编程式事务因异常回滚: {}", e.getMessage());
            if (connection != null) {
                try {
                    transactionManager.rollback(connection);
                } catch (Exception rollbackEx) {
                    log.warn("回滚失败: {}", rollbackEx.getMessage());
                }
            }
            throw new RuntimeException("Transaction failed: " + e.getMessage(), e);
        }
    }
}
