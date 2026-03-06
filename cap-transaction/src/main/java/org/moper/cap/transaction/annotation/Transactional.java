package org.moper.cap.transaction.annotation;

import java.lang.annotation.*;

/**
 * 事务注解
 *
 * 标注在方法或类上表示需要事务管理。
 * 标注在类上时，类中所有公共方法均受事务管理。
 *
 * 使用示例：
 * <pre>{@code
 * @Transactional
 * public void transfer(long fromId, long toId, BigDecimal amount) {
 *     // 业务逻辑
 * }
 * }</pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Transactional {

    /**
     * 事务超时时间（秒），-1 表示不超时。
     *
     * <p>注意：当前版本通过在事务提交前检查已用时间来实现超时检测。
     * 若方法执行时间超过设定值，将在提交阶段抛出 {@link org.moper.cap.transaction.exception.TransactionException} 并触发回滚。
     */
    int timeout() default -1;

    /**
     * 是否为只读事务
     */
    boolean readOnly() default false;

    /**
     * 事务隔离级别
     */
    IsolationLevel isolation() default IsolationLevel.READ_COMMITTED;

    /**
     * 事务传播性
     */
    Propagation propagation() default Propagation.REQUIRED;

    /**
     * 需要回滚的异常类型
     */
    Class<? extends Exception>[] rollbackFor() default {};

    /**
     * 不需要回滚的异常类型
     */
    Class<? extends Exception>[] noRollbackFor() default {};
}
