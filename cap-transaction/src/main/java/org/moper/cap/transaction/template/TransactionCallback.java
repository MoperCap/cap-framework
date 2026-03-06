package org.moper.cap.transaction.template;

import org.moper.cap.transaction.context.TransactionStatus;

/**
 * 事务回调接口
 *
 * <p>用于编程式事务中，定义在事务内执行的业务逻辑。
 *
 * <p>使用示例：
 * <pre>{@code
 * txTemplate.execute(status -> {
 *     // 业务逻辑
 *     if (someCondition) {
 *         status.setRollbackOnly();  // 标记回滚
 *     }
 *     return result;
 * });
 * }</pre>
 *
 * @param <T> 业务逻辑返回值类型
 */
@FunctionalInterface
public interface TransactionCallback<T> {

    /**
     * 在事务中执行业务逻辑
     *
     * @param status 当前事务状态，可通过 {@link TransactionStatus#setRollbackOnly()} 标记事务只回滚
     * @return 业务逻辑的返回值
     * @throws Exception 业务异常或数据库异常
     */
    T doInTransaction(TransactionStatus status) throws Exception;
}
