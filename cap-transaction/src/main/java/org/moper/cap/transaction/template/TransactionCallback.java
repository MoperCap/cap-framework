package org.moper.cap.transaction.template;

import org.moper.cap.transaction.context.TransactionStatus;

/**
 * 编程式事务回调接口，由业务代码实现，在事务上下文中执行。
 *
 * <p>通过 {@link TransactionStatus} 参数可将当前事务标记为只回滚（{@link TransactionStatus#setRollbackOnly()}）。
 *
 * @param <T> 业务操作的返回类型
 */
@FunctionalInterface
public interface TransactionCallback<T> {

    /**
     * 在事务上下文中执行业务逻辑并返回结果。
     *
     * <p>若需强制回滚，可调用 {@code status.setRollbackOnly()}；
     * 若此方法抛出异常，事务将根据 {@link org.moper.cap.transaction.annotation.Transactional#rollbackFor()} 的规则决定是否回滚。
     *
     * @param status 当前事务状态，用于控制回滚行为
     * @return 业务操作结果
     * @throws Exception 业务异常
     */
    T doInTransaction(TransactionStatus status) throws Exception;
}
