package org.moper.cap.transaction.aspect;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.aop.annotation.Around;
import org.moper.cap.aop.annotation.Aspect;
import org.moper.cap.aop.model.ProceedingJoinPoint;
import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.transaction.annotation.IsolationLevel;
import org.moper.cap.transaction.annotation.Propagation;
import org.moper.cap.transaction.annotation.Transactional;
import org.moper.cap.transaction.context.TransactionContext;
import org.moper.cap.transaction.manager.TransactionManager;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Savepoint;

/**
 * 声明式事务切面，拦截所有标注了 {@link Transactional} 的方法，
 * 根据传播性和其他属性自动管理事务生命周期。
 *
 * <p>支持全部 7 种事务传播性：
 * <ul>
 *   <li>{@link Propagation#REQUIRED} — 加入已有事务或新建</li>
 *   <li>{@link Propagation#SUPPORTS} — 加入已有事务或无事务执行</li>
 *   <li>{@link Propagation#MANDATORY} — 必须在已有事务中执行</li>
 *   <li>{@link Propagation#REQUIRES_NEW} — 挂起已有事务并新建</li>
 *   <li>{@link Propagation#NOT_SUPPORTED} — 挂起已有事务并无事务执行</li>
 *   <li>{@link Propagation#NEVER} — 必须在无事务环境中执行</li>
 *   <li>{@link Propagation#NESTED} — 在已有事务中使用 Savepoint 嵌套执行</li>
 * </ul>
 *
 * <p>此 Aspect 实例由 {@link org.moper.cap.transaction.runner.TransactionBootstrapRunner}
 * 在 AopBootstrapRunner 运行前注册到 Bean 容器，从而被 AOP 框架感知和应用。
 */
@Slf4j
@Aspect
public class TransactionAspect {

    private final BeanContainer beanContainer;

    public TransactionAspect(BeanContainer beanContainer) {
        this.beanContainer = beanContainer;
    }

    /**
     * 拦截所有标注了 {@link Transactional} 的方法，施加事务管理逻辑。
     *
     * @param pjp 环绕连接点
     * @return 目标方法的返回值
     * @throws Throwable 目标方法或事务操作抛出的异常
     */
    @Around("@method(org.moper.cap.transaction.annotation.Transactional)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Transactional txAnnotation = getTransactionalAnnotation(pjp);
        if (txAnnotation == null) {
            return pjp.proceed();
        }

        TransactionManager txManager = resolveTransactionManager();
        if (txManager == null) {
            log.warn("未找到 TransactionManager Bean，方法 [{}] 将以无事务方式执行",
                    pjp.getMethod().getName());
            return pjp.proceed();
        }

        TransactionContext.TransactionInfo existingTx = TransactionContext.getCurrentTransaction();
        Propagation propagation = txAnnotation.propagation();

        log.debug("事务切面: method={}, propagation={}, existingTx={}",
                pjp.getMethod().getName(), propagation, existingTx != null);

        return switch (propagation) {
            case REQUIRED      -> executeRequired(pjp, txAnnotation, txManager, existingTx);
            case SUPPORTS      -> executeSupports(pjp, txAnnotation, txManager, existingTx);
            case MANDATORY     -> executeMandatory(pjp, txAnnotation, txManager, existingTx);
            case REQUIRES_NEW  -> executeRequiresNew(pjp, txAnnotation, txManager, existingTx);
            case NOT_SUPPORTED -> executeNotSupported(pjp, existingTx, txManager);
            case NEVER         -> executeNever(pjp, existingTx);
            case NESTED        -> executeNested(pjp, txAnnotation, txManager, existingTx);
        };
    }

    // ─────────────────────────────────────── Propagation Handlers ───────────────────────────────────────

    /**
     * REQUIRED：加入已有事务；若无则新建事务。
     */
    private Object executeRequired(ProceedingJoinPoint pjp, Transactional txAnnotation,
                                   TransactionManager txManager,
                                   TransactionContext.TransactionInfo existingTx) throws Throwable {
        if (existingTx != null) {
            return joinExistingTransaction(pjp, txAnnotation, existingTx);
        }
        return executeInNewTransaction(pjp, txAnnotation, txManager);
    }

    /**
     * SUPPORTS：加入已有事务；若无则无事务执行。
     */
    private Object executeSupports(ProceedingJoinPoint pjp, Transactional txAnnotation,
                                   TransactionManager txManager,
                                   TransactionContext.TransactionInfo existingTx) throws Throwable {
        if (existingTx != null) {
            return joinExistingTransaction(pjp, txAnnotation, existingTx);
        }
        log.debug("SUPPORTS: 无活跃事务，以无事务方式执行");
        return pjp.proceed();
    }

    /**
     * MANDATORY：必须在已有事务中执行；若无则抛出异常。
     */
    private Object executeMandatory(ProceedingJoinPoint pjp, Transactional txAnnotation,
                                    TransactionManager txManager,
                                    TransactionContext.TransactionInfo existingTx) throws Throwable {
        if (existingTx == null) {
            throw new IllegalTransactionStateException(
                    "MANDATORY: 当前无活跃事务，方法 [" + pjp.getMethod().getName() + "] 要求在已有事务中执行");
        }
        return joinExistingTransaction(pjp, txAnnotation, existingTx);
    }

    /**
     * REQUIRES_NEW：挂起已有事务（若有），始终新建事务。
     */
    private Object executeRequiresNew(ProceedingJoinPoint pjp, Transactional txAnnotation,
                                      TransactionManager txManager,
                                      TransactionContext.TransactionInfo existingTx) throws Throwable {
        TransactionContext.TransactionInfo suspendedTx = null;
        if (existingTx != null) {
            log.debug("REQUIRES_NEW: 挂起现有事务");
            suspendedTx = TransactionContext.suspendTransaction();
        }
        try {
            return executeInNewTransaction(pjp, txAnnotation, txManager);
        } finally {
            if (suspendedTx != null) {
                log.debug("REQUIRES_NEW: 恢复被挂起的事务");
                TransactionContext.resumeTransaction(suspendedTx);
            }
        }
    }

    /**
     * NOT_SUPPORTED：挂起已有事务（若有），以无事务方式执行。
     */
    private Object executeNotSupported(ProceedingJoinPoint pjp,
                                       TransactionContext.TransactionInfo existingTx,
                                       TransactionManager txManager) throws Throwable {
        TransactionContext.TransactionInfo suspendedTx = null;
        if (existingTx != null) {
            log.debug("NOT_SUPPORTED: 挂起现有事务");
            suspendedTx = TransactionContext.suspendTransaction();
        }
        try {
            log.debug("NOT_SUPPORTED: 以无事务方式执行");
            return pjp.proceed();
        } finally {
            if (suspendedTx != null) {
                log.debug("NOT_SUPPORTED: 恢复被挂起的事务");
                TransactionContext.resumeTransaction(suspendedTx);
            }
        }
    }

    /**
     * NEVER：若存在活跃事务则抛出异常；否则无事务执行。
     */
    private Object executeNever(ProceedingJoinPoint pjp,
                                TransactionContext.TransactionInfo existingTx) throws Throwable {
        if (existingTx != null) {
            throw new IllegalTransactionStateException(
                    "NEVER: 当前存在活跃事务，方法 [" + pjp.getMethod().getName() + "] 不允许在事务中执行");
        }
        log.debug("NEVER: 以无事务方式执行");
        return pjp.proceed();
    }

    /**
     * NESTED：若存在活跃事务则使用 Savepoint 嵌套执行；否则与 REQUIRED 相同，新建事务。
     */
    private Object executeNested(ProceedingJoinPoint pjp, Transactional txAnnotation,
                                 TransactionManager txManager,
                                 TransactionContext.TransactionInfo existingTx) throws Throwable {
        if (existingTx == null) {
            log.debug("NESTED: 无活跃事务，按 REQUIRED 逻辑新建事务");
            return executeInNewTransaction(pjp, txAnnotation, txManager);
        }

        Connection connection = existingTx.getConnection();
        Savepoint savepoint;
        try {
            savepoint = txManager.createSavepoint(connection);
            log.debug("NESTED: 创建 Savepoint 成功");
        } catch (UnsupportedOperationException e) {
            log.warn("NESTED: 数据库不支持 Savepoint，降级为 REQUIRED 语义");
            return joinExistingTransaction(pjp, txAnnotation, existingTx);
        }

        try {
            Object result = pjp.proceed();
            txManager.releaseSavepoint(connection, savepoint);
            log.debug("NESTED: Savepoint 已释放");
            return result;
        } catch (Throwable t) {
            if (shouldRollback(txAnnotation, t)) {
                try {
                    txManager.rollbackToSavepoint(connection, savepoint);
                    log.debug("NESTED: 已回滚至 Savepoint");
                } catch (Exception rollbackEx) {
                    log.warn("NESTED: 回滚至 Savepoint 失败: {}", rollbackEx.getMessage());
                }
            } else {
                try {
                    txManager.releaseSavepoint(connection, savepoint);
                } catch (Exception releaseEx) {
                    log.debug("NESTED: 释放 Savepoint 失败: {}", releaseEx.getMessage());
                }
            }
            throw t;
        }
    }

    // ─────────────────────────────────────── Core Transaction Execution ───────────────────────────────────────

    /**
     * 加入已有事务执行：仅调用目标方法，不开启/提交/回滚事务；
     * 异常时根据注解设置将外层事务标记为 rollbackOnly。
     */
    private Object joinExistingTransaction(ProceedingJoinPoint pjp, Transactional txAnnotation,
                                           TransactionContext.TransactionInfo existingTx) throws Throwable {
        try {
            return pjp.proceed();
        } catch (Throwable t) {
            if (shouldRollback(txAnnotation, t)) {
                log.debug("加入已有事务时发生异常，将外层事务标记为 rollbackOnly: {}", t.getMessage());
                existingTx.setRollbackOnly(true);
            }
            throw t;
        }
    }

    /**
     * 在新事务中执行：开启事务 → 执行目标方法 → 提交/回滚 → 关闭事务。
     */
    private Object executeInNewTransaction(ProceedingJoinPoint pjp, Transactional txAnnotation,
                                           TransactionManager txManager) throws Throwable {
        IsolationLevel isolation = txAnnotation.isolation();
        boolean readOnly = txAnnotation.readOnly();
        int timeout = txAnnotation.timeout();
        long timeoutMillis = timeout > 0 ? (long) timeout * 1000 : -1;
        long startTime = System.currentTimeMillis();

        Connection connection = txManager.beginTransaction(readOnly, isolation);
        log.debug("新事务已开启: method={}, isolation={}, readOnly={}, timeout={}s",
                pjp.getMethod().getName(), isolation, readOnly, timeout);

        try {
            Object result = pjp.proceed();

            // 超时检测
            if (timeoutMillis > 0) {
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed > timeoutMillis) {
                    log.warn("事务超时: method={}, elapsed={}ms, timeout={}s",
                            pjp.getMethod().getName(), elapsed, timeout);
                    txManager.rollback(connection);
                    throw new TransactionTimeoutException(
                            "事务超时: 方法 [" + pjp.getMethod().getName() + "] 执行耗时 "
                                    + elapsed + "ms，超过设定的 " + timeout + "s");
                }
            }

            // 检查 rollbackOnly 标志（内层方法可能已设置）
            TransactionContext.TransactionInfo txInfo = TransactionContext.getCurrentTransaction();
            if (txInfo != null && txInfo.isRollbackOnly()) {
                log.debug("事务被标记为 rollbackOnly，执行回滚");
                txManager.rollback(connection);
            } else {
                txManager.commit(connection);
                log.debug("事务已提交: method={}, elapsed={}ms",
                        pjp.getMethod().getName(), System.currentTimeMillis() - startTime);
            }

            return result;

        } catch (Throwable t) {
            if (shouldRollback(txAnnotation, t)) {
                log.debug("事务因异常回滚: method={}, exception={}",
                        pjp.getMethod().getName(), t.getMessage());
                try {
                    txManager.rollback(connection);
                } catch (Exception rollbackEx) {
                    log.warn("回滚事务失败: {}", rollbackEx.getMessage());
                }
            } else {
                log.debug("异常类型不触发回滚，执行提交: exception={}", t.getMessage());
                try {
                    txManager.commit(connection);
                } catch (Exception commitEx) {
                    log.warn("提交事务失败: {}", commitEx.getMessage());
                }
            }
            throw t;
        }
    }

    // ─────────────────────────────────────── Helper Methods ───────────────────────────────────────

    /**
     * 判断给定异常是否应触发事务回滚。
     *
     * <p>判断顺序：
     * <ol>
     *   <li>{@link Transactional#noRollbackFor()} 中命中 → 不回滚（优先级最高）</li>
     *   <li>{@link Transactional#rollbackFor()} 中命中 → 回滚</li>
     *   <li>默认规则：{@link RuntimeException} 或 {@link Error} → 回滚；其他 → 不回滚</li>
     * </ol>
     */
    private boolean shouldRollback(Transactional txAnnotation, Throwable t) {
        for (Class<? extends Throwable> noRollbackClass : txAnnotation.noRollbackFor()) {
            if (noRollbackClass.isInstance(t)) {
                return false;
            }
        }
        for (Class<? extends Throwable> rollbackClass : txAnnotation.rollbackFor()) {
            if (rollbackClass.isInstance(t)) {
                return true;
            }
        }
        return (t instanceof RuntimeException) || (t instanceof Error);
    }

    /**
     * 从方法或目标类上解析 {@link Transactional} 注解。
     *
     * <p>查找顺序：
     * <ol>
     *   <li>代理方法上的注解</li>
     *   <li>目标类层级中对应方法上的注解</li>
     *   <li>目标类上的类级注解</li>
     * </ol>
     */
    private Transactional getTransactionalAnnotation(ProceedingJoinPoint pjp) {
        Method method = pjp.getMethod();

        // 1. 直接检查代理方法（Javassist 代理的方法可能继承了注解）
        Transactional tx = method.getAnnotation(Transactional.class);
        if (tx != null) {
            return tx;
        }

        // 2. 在目标类层级中查找对应方法上的注解
        Class<?> targetClass = pjp.getTarget().getClass();
        Method targetMethod = findMethod(targetClass, method.getName(), method.getParameterTypes());
        if (targetMethod != null) {
            tx = targetMethod.getAnnotation(Transactional.class);
            if (tx != null) {
                return tx;
            }
        }

        // 3. 类级注解
        return targetClass.getAnnotation(Transactional.class);
    }

    /**
     * 从 Bean 容器中懒加载 {@link TransactionManager}。
     * 若未找到则返回 {@code null}，不抛出异常。
     */
    private TransactionManager resolveTransactionManager() {
        try {
            return beanContainer.getBean(TransactionManager.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 在目标类及其父类层级中查找指定方法。
     */
    private static Method findMethod(Class<?> clazz, String name, Class<?>[] paramTypes) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                Method m = current.getDeclaredMethod(name, paramTypes);
                m.setAccessible(true);
                return m;
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    // ─────────────────────────────────────── Exception Types ───────────────────────────────────────

    /**
     * 事务状态非法异常（如 MANDATORY 未在事务中调用、NEVER 在事务中调用）。
     */
    public static class IllegalTransactionStateException extends RuntimeException {
        public IllegalTransactionStateException(String message) {
            super(message);
        }
    }

    /**
     * 事务超时异常。
     */
    public static class TransactionTimeoutException extends RuntimeException {
        public TransactionTimeoutException(String message) {
            super(message);
        }
    }
}
