package org.moper.cap.transaction.annotation;

import java.lang.annotation.*;

/**
 * 声明式事务注解，可标注在方法或类上。
 *
 * <p>标注在<b>类</b>上时，该类所有 public 方法均受该注解约束（方法级注解优先级更高）。
 * 标注在<b>方法</b>上时，仅该方法受约束。
 *
 * <p>框架将通过 AOP 拦截所有带有此注解的方法，自动管理事务的开始、提交和回滚。
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Transactional {

    /**
     * 事务传播性，默认为 {@link Propagation#REQUIRED}。
     */
    Propagation propagation() default Propagation.REQUIRED;

    /**
     * 事务隔离级别，默认为 {@link IsolationLevel#DEFAULT}（使用数据源默认值）。
     */
    IsolationLevel isolation() default IsolationLevel.DEFAULT;

    /**
     * 事务超时时间（秒），{@code -1} 表示不设超时。
     */
    int timeout() default -1;

    /**
     * 是否为只读事务，默认 {@code false}。
     */
    boolean readOnly() default false;

    /**
     * 触发回滚的异常类型（默认仅 {@link RuntimeException} 和 {@link Error} 触发回滚）。
     */
    Class<? extends Throwable>[] rollbackFor() default {};

    /**
     * 不触发回滚的异常类型（优先级高于 {@link #rollbackFor()}）。
     */
    Class<? extends Throwable>[] noRollbackFor() default {};
}
