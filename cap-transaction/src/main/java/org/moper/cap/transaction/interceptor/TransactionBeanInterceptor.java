package org.moper.cap.transaction.interceptor;

import javassist.util.proxy.ProxyFactory;
import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.exception.BeanException;
import org.moper.cap.bean.interceptor.BeanInterceptor;
import org.moper.cap.transaction.annotation.Propagation;
import org.moper.cap.transaction.annotation.Transactional;
import org.moper.cap.transaction.context.TransactionContext;
import org.moper.cap.transaction.exception.TransactionException;
import org.moper.cap.transaction.manager.TransactionManager;
import sun.misc.Unsafe;

import java.lang.reflect.*;
import java.sql.Connection;

/**
 * 事务 Bean 拦截器
 *
 * <p>在 Bean 创建阶段检测是否存在 {@link Transactional} 注解的方法，
 * 若存在则为其创建代理，代理负责在方法执行前后进行事务管理。
 *
 * <p>支持：
 * <ul>
 *   <li>JDK 动态代理（Bean 实现了接口时）</li>
 *   <li>Javassist 子类代理（Bean 未实现接口时）</li>
 *   <li>嵌套事务（通过 {@link TransactionContext} 的 Stack 实现）</li>
 *   <li>事务传播性（REQUIRED, REQUIRES_NEW, NESTED, NEVER, NOT_SUPPORTED, MANDATORY, SUPPORTS）</li>
 *   <li>灵活的回滚规则（rollbackFor, noRollbackFor）</li>
 * </ul>
 *
 * <p>本拦截器的 {@link #getOrder()} 为 500，在 AOP 拦截器（400）之后执行，
 * 以确保事务代理包裹 AOP 代理。
 */
@Slf4j
public class TransactionBeanInterceptor implements BeanInterceptor {

    private static final Unsafe UNSAFE = getUnsafe();

    private final TransactionManager transactionManager;

    public TransactionBeanInterceptor(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public Object afterPropertyInjection(Object bean, BeanDefinition definition) throws BeanException {
        if (!hasTransactionalMethods(bean.getClass())) {
            return bean;
        }
        log.debug("为 Bean [{}] 创建事务代理", definition.name());
        return createProxy(bean);
    }

    /**
     * 检查类（及其父类层级）是否含有 {@link Transactional} 注解的方法。
     */
    private boolean hasTransactionalMethods(Class<?> clazz) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Method m : current.getDeclaredMethods()) {
                if (m.isAnnotationPresent(Transactional.class)) {
                    return true;
                }
            }
            current = current.getSuperclass();
        }
        return false;
    }

    /**
     * 根据 Bean 类型选择合适的代理策略。
     *
     * <ul>
     *   <li>若 Bean 实现了接口：使用 JDK 动态代理</li>
     *   <li>否则：使用 Javassist 子类代理</li>
     * </ul>
     */
    private Object createProxy(Object target) {
        Class<?> targetClass = target.getClass();
        Class<?>[] interfaces = getNonInternalInterfaces(targetClass);

        if (interfaces.length > 0) {
            return createJdkProxy(target, targetClass, interfaces);
        } else {
            return createJavassistProxy(target, targetClass);
        }
    }

    /**
     * 获取类实现的非 JDK 内部接口列表（排除 java.* / sun.* 等内部接口）。
     */
    private Class<?>[] getNonInternalInterfaces(Class<?> clazz) {
        java.util.function.Predicate<Class<?>> isUserDefined =
                i -> !i.getName().startsWith("java.") && !i.getName().startsWith("sun.");
        return java.util.Arrays.stream(clazz.getInterfaces())
                .filter(isUserDefined)
                .toArray(Class<?>[]::new);
    }

    private Object createJdkProxy(Object target, Class<?> targetClass, Class<?>[] interfaces) {
        return Proxy.newProxyInstance(
                targetClass.getClassLoader(),
                interfaces,
                new TransactionInvocationHandler(target, targetClass, transactionManager)
        );
    }

    private Object createJavassistProxy(Object target, Class<?> targetClass) {
        try {
            ProxyFactory factory = new ProxyFactory();
            factory.setSuperclass(targetClass);
            factory.setFilter(m -> {
                int mod = m.getModifiers();
                return !Modifier.isAbstract(mod)
                        && !Modifier.isNative(mod)
                        && !Modifier.isStatic(mod);
            });

            Class<?> proxyClass = factory.createClass();
            Object proxyInstance = UNSAFE.allocateInstance(proxyClass);

            ((javassist.util.proxy.Proxy) proxyInstance).setHandler((self, thisMethod, proceed, args) -> {
                // thisMethod is declared in targetClass (the immediate superclass)
                Transactional tx = findTransactionalAnnotation(thisMethod);
                if (tx == null) {
                    thisMethod.setAccessible(true);
                    return thisMethod.invoke(target, args);
                }
                return executeWithTransaction(target, thisMethod, args, tx);
            });

            return proxyInstance;
        } catch (Exception e) {
            throw new BeanException("Failed to create Javassist transaction proxy for " + targetClass.getName(), e);
        }
    }

    /**
     * 事务感知的 JDK 调用处理器。
     */
    private static class TransactionInvocationHandler implements InvocationHandler {

        private final Object target;
        private final Class<?> targetClass;
        private final TransactionManager transactionManager;

        TransactionInvocationHandler(Object target, Class<?> targetClass, TransactionManager transactionManager) {
            this.target = target;
            this.targetClass = targetClass;
            this.transactionManager = transactionManager;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // Find the corresponding method on the target class (not the interface)
            Transactional tx = findTransactionalOnTargetClass(method, targetClass);
            if (tx == null) {
                method.setAccessible(true);
                return method.invoke(target, args);
            }
            return executeWithTransaction(target, method, args, tx, transactionManager);
        }

        private Transactional findTransactionalOnTargetClass(Method interfaceMethod, Class<?> targetClass) {
            Class<?> current = targetClass;
            while (current != null && current != Object.class) {
                try {
                    Method m = current.getDeclaredMethod(interfaceMethod.getName(), interfaceMethod.getParameterTypes());
                    Transactional tx = m.getAnnotation(Transactional.class);
                    if (tx != null) return tx;
                } catch (NoSuchMethodException e) {
                    // not in this class, try superclass
                }
                current = current.getSuperclass();
            }
            return null;
        }
    }

    /**
     * 在方法及其父类层级中查找 {@link Transactional} 注解。
     *
     * <p>Javassist 代理的 {@code thisMethod} 声明在直接父类（可能是 AOP 代理类）中，
     * AOP 代理类的方法没有 {@link Transactional} 注解，因此需要向上遍历到原始类。
     */
    private static Transactional findTransactionalAnnotation(Method method) {
        Transactional tx = method.getAnnotation(Transactional.class);
        if (tx != null) return tx;

        Class<?> superClass = method.getDeclaringClass().getSuperclass();
        while (superClass != null && superClass != Object.class) {
            try {
                Method superMethod = superClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
                tx = superMethod.getAnnotation(Transactional.class);
                if (tx != null) return tx;
            } catch (NoSuchMethodException e) {
                // not declared in this superclass
            }
            superClass = superClass.getSuperclass();
        }
        return null;
    }

    /**
     * 在事务上下文中执行目标方法（Javassist 代理入口）。
     */
    private Object executeWithTransaction(Object target, Method method, Object[] args, Transactional tx) throws Throwable {
        return executeWithTransaction(target, method, args, tx, transactionManager);
    }

    /**
     * 核心事务执行逻辑：
     * <ol>
     *   <li>根据传播性决定是否开启新事务</li>
     *   <li>执行目标方法</li>
     *   <li>成功则提交；异常则根据回滚规则决定回滚或提交</li>
     * </ol>
     */
    static Object executeWithTransaction(Object target, Method method, Object[] args,
                                         Transactional tx, TransactionManager transactionManager) throws Throwable {
        Propagation propagation = tx.propagation();
        TransactionContext.TransactionInfo currentTx = TransactionContext.getCurrentTransaction();

        // Handle propagation modes that don't require starting a transaction
        switch (propagation) {
            case NEVER:
                if (currentTx != null) {
                    throw new TransactionException(
                            "NEVER propagation: transaction already exists for method " + method.getName());
                }
                method.setAccessible(true);
                return invokeTarget(target, method, args);

            case NOT_SUPPORTED:
                // Suspend current transaction (not truly suspended here, just proceed without one)
                log.debug("NOT_SUPPORTED: 不在事务中执行方法 {}", method.getName());
                method.setAccessible(true);
                return invokeTarget(target, method, args);

            case MANDATORY:
                if (currentTx == null) {
                    throw new TransactionException(
                            "MANDATORY propagation: no transaction exists for method " + method.getName());
                }
                method.setAccessible(true);
                return invokeTarget(target, method, args);

            case SUPPORTS:
                // Execute with or without transaction, no new transaction created
                method.setAccessible(true);
                return invokeTarget(target, method, args);

            default:
                // REQUIRED, REQUIRES_NEW, NESTED — need transaction management
                break;
        }

        // Begin transaction (respects propagation via TransactionContext)
        Connection connection = transactionManager.beginTransaction(tx.readOnly(), tx.isolation());

        try {
            method.setAccessible(true);
            Object result = invokeTarget(target, method, args);

            // Check timeout before committing
            checkTimeout(tx);

            transactionManager.commit(connection);
            return result;
        } catch (Throwable t) {
            if (shouldRollback(tx, t)) {
                log.debug("回滚事务: method={}, exception={}", method.getName(), t.getClass().getName());
                transactionManager.rollback(connection);
            } else {
                log.debug("提交事务（异常不触发回滚）: method={}, exception={}", method.getName(), t.getClass().getName());
                transactionManager.commit(connection);
            }
            throw t;
        }
    }

    /**
     * 调用目标方法，将 {@link InvocationTargetException} 解包为原始异常。
     */
    private static Object invokeTarget(Object target, Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException ite) {
            throw ite.getCause() != null ? ite.getCause() : ite;
        }
    }

    /**
     * 检查事务是否超时（仅当 {@link Transactional#timeout()} &gt; 0 时有效）。
     *
     * <p>在方法执行完成、提交之前检查已用时间；若超时则抛出
     * {@link TransactionException} 触发回滚。
     */
    private static void checkTimeout(Transactional tx) {
        int timeout = tx.timeout();
        if (timeout <= 0) return;

        TransactionContext.TransactionInfo txInfo = TransactionContext.getCurrentTransaction();
        if (txInfo == null) return;

        long elapsedSeconds = (System.currentTimeMillis() - txInfo.getStartTime()) / 1000;
        if (elapsedSeconds >= timeout) {
            throw new TransactionException(
                    "Transaction timed out after " + elapsedSeconds + "s (limit: " + timeout + "s)");
        }
    }

    /**
     * 根据 {@link Transactional} 配置判断给定异常是否应触发回滚。
     *
     * <p>规则（按优先级）：
     * <ol>
     *   <li>{@code noRollbackFor} 匹配 → 不回滚</li>
     *   <li>{@code rollbackFor} 非空且匹配 → 回滚</li>
     *   <li>{@code rollbackFor} 非空但不匹配 → 不回滚</li>
     *   <li>默认：{@link RuntimeException} 及其子类回滚，{@link Error} 也回滚</li>
     * </ol>
     */
    static boolean shouldRollback(Transactional tx, Throwable t) {
        if (!(t instanceof Exception exception)) {
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

        return t instanceof RuntimeException;
    }

    @Override
    public int getOrder() {
        return 500;
    }

    private static Unsafe getUnsafe() {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            return (Unsafe) unsafeField.get(null);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
