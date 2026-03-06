package org.moper.cap.transaction.template;

/**
 * 事务回调接口
 *
 * <p>用于编程式事务中，定义在事务内执行的业务逻辑
 *
 * @param <T> 业务逻辑返回值类型
 */
@FunctionalInterface
public interface TransactionCallback<T> {

    /**
     * 在事务中执行业务逻辑
     *
     * @return 业务逻辑的返回值
     * @throws Exception 业务异常或数据库异常
     */
    T doInTransaction() throws Exception;
}
