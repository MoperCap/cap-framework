package org.moper.cap.transaction.template;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.transaction.annotation.IsolationLevel;
import org.moper.cap.transaction.annotation.Propagation;
import org.moper.cap.transaction.context.TransactionContext;
import org.moper.cap.transaction.manager.TransactionManager;

import java.sql.Connection;

/**
 * 事务模板 - 提供编程式事务 API
 *
 * <p>允许在任何地方使用事务，无需 {@code @Transactional} 注解。
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
    // NOTE: propagation and timeout are reserved for future implementation.
    // Currently execute() implements REQUIRED semantics: join existing transaction
    // or begin a new one.
    private Propagation propagation = Propagation.REQUIRED;
    private int timeout = -1;

    public TransactionTemplate(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * 执行事务内的业务逻辑。
     *
     * <p>若当前已存在事务，则加入已有事务（REQUIRED 语义）；
     * 否则开启新事务，执行完成后提交，异常时回滚。
     *
     * @param action 事务内的业务逻辑回调
     * @param <T>    返回值类型
     * @return 业务逻辑的返回值
     * @throws RuntimeException 业务逻辑抛出异常时封装后重新抛出
     */
    public <T> T execute(TransactionCallback<T> action) {
        log.debug("开始编程式事务执行");

        // 检查是否已在事务中
        TransactionContext.TransactionInfo currentTx = TransactionContext.getCurrentTransaction();
        boolean isNew = (currentTx == null);

        Connection conn = null;
        try {
            if (isNew) {
                conn = transactionManager.beginTransaction(readOnly, isolationLevel);
                log.debug("编程式事务已开始");
            } else {
                conn = currentTx.getConnection();
                log.debug("加入现有事务，深度: {}", TransactionContext.getTransactionDepth());
            }

            T result = action.doInTransaction();

            if (isNew) {
                transactionManager.commit(conn);
                log.debug("编程式事务已提交");
            }

            return result;
        } catch (Exception e) {
            if (isNew && conn != null) {
                try {
                    transactionManager.rollback(conn);
                    log.debug("编程式事务已回滚");
                } catch (Exception rollbackEx) {
                    log.error("回滚失败", rollbackEx);
                }
            }
            throw new RuntimeException("Transaction execution failed", e);
        }
    }

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
