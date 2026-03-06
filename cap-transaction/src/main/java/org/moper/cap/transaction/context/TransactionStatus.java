package org.moper.cap.transaction.context;

/**
 * 编程式事务的状态对象，通过 {@link org.moper.cap.transaction.template.TransactionCallback} 传递给业务代码。
 *
 * <p>业务代码可在任意位置调用 {@link #setRollbackOnly()} 将当前事务标记为只回滚，
 * 框架将在回调返回后自动执行回滚而不是提交。
 */
public class TransactionStatus {

    private boolean rollbackOnly = false;

    /**
     * 将当前事务标记为只回滚。
     * 调用此方法后，无论业务逻辑是否抛出异常，事务均会被回滚。
     */
    public void setRollbackOnly() {
        this.rollbackOnly = true;
    }

    /**
     * 返回当前事务是否已被标记为只回滚。
     */
    public boolean isRollbackOnly() {
        return rollbackOnly;
    }
}
