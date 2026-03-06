package org.moper.cap.transaction.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.transaction.annotation.Transactional;
import org.moper.cap.transaction.aspect.TransactionAspect;
import org.moper.cap.transaction.manager.TransactionManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 事务方法的调用处理器 - 用于 JDK 动态代理
 *
 * <p>当目标 Bean 实现了接口时，由 {@link TransactionBeanInterceptor} 创建 JDK 代理，
 * 本处理器负责在代理方法被调用时：
 * <ol>
 *   <li>查找目标实现类上是否存在 {@link Transactional} 注解</li>
 *   <li>若存在，在运行时从容器查找 {@link TransactionManager} 实现</li>
 *   <li>委托 {@link TransactionAspect} 进行事务生命周期管理</li>
 *   <li>若找不到 {@link TransactionManager}，直接透传到目标对象</li>
 * </ol>
 *
 * <p>设计特点：
 * <ul>
 *   <li>缓存 {@link TransactionManager} 避免重复查找</li>
 *   <li>如果找不到 {@link TransactionManager}，直接调用方法（不使用事务）</li>
 * </ul>
 */
@Slf4j
public class TransactionInvocationHandler implements InvocationHandler {

    private final Object target;
    private final Class<?> targetClass;
    private final BeanContainer beanContainer;

    // 缓存 TransactionManager，避免每次都查找
    private volatile TransactionManager cachedTransactionManager;
    private volatile boolean transactionManagerLookupAttempted = false;

    // 缓存 TransactionAspect
    private volatile TransactionAspect cachedTransactionAspect;

    public TransactionInvocationHandler(Object target, Class<?> targetClass, BeanContainer beanContainer) {
        this.target = target;
        this.targetClass = targetClass;
        this.beanContainer = beanContainer;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Find the corresponding method on the target class (which may have @Transactional)
        Method targetMethod = resolveAnnotatedMethod(method);
        if (targetMethod == null) {
            // No @Transactional found, bypass transaction management
            return method.invoke(target, args);
        }

        log.debug("事务代理拦截: method={}", method.getName());

        // 运行时获取 TransactionManager 实现
        TransactionManager txManager = getTransactionManager();

        if (txManager == null) {
            // TransactionManager 不可用，直接调用目标方法
            log.warn("⚠️  未找到 TransactionManager 实现，方法 [{}] 将在无事务环境下执行", method.getName());
            log.warn("💡 提示：请确保已在应用中配置 TransactionManager Bean");
            return method.invoke(target, args);
        }

        TransactionAspect aspect = getTransactionAspect(txManager);

        try {
            // 1. 事务开始
            aspect.handleTransactionBegin(targetMethod);

            // 2. 调用目标方法
            Object result = method.invoke(target, args);

            // 3. 事务提交（正常流程）
            aspect.handleTransactionEnd(targetMethod, null);

            return result;
        } catch (InvocationTargetException ite) {
            Throwable cause = ite.getCause() != null ? ite.getCause() : ite;
            // 4. 捕获异常，进行事务回滚或提交
            aspect.handleTransactionEnd(targetMethod, cause);
            throw cause;
        } catch (Exception e) {
            aspect.handleTransactionEnd(targetMethod, e);
            throw e;
        }
    }

    /**
     * 获取 TransactionManager 实现，缓存查找结果以提高性能。
     *
     * <p>使用双重检查锁定模式（double-checked locking）保证线程安全：
     * 外部检查 {@code cachedTransactionManager}（volatile 字段），
     * 仅在需要时进入同步块以完成懒加载。
     */
    private TransactionManager getTransactionManager() {
        if (cachedTransactionManager != null) {
            return cachedTransactionManager;
        }

        synchronized (this) {
            // Re-check inside synchronized block (double-checked locking)
            if (cachedTransactionManager != null) {
                return cachedTransactionManager;
            }
            if (transactionManagerLookupAttempted) {
                // 已经尝试过查找但失败，直接返回 null
                return null;
            }

            try {
                cachedTransactionManager = beanContainer.getBean(TransactionManager.class);
                log.debug("✅ 找到 TransactionManager 实现: {}",
                         cachedTransactionManager.getClass().getSimpleName());
            } catch (Exception e) {
                log.debug("❌ 未找到 TransactionManager 实现: {}", e.getMessage());
            }
            transactionManagerLookupAttempted = true;
        }

        return cachedTransactionManager;
    }

    /**
     * 获取或创建 TransactionAspect。
     */
    private TransactionAspect getTransactionAspect(TransactionManager txManager) {
        if (cachedTransactionAspect == null) {
            synchronized (this) {
                if (cachedTransactionAspect == null) {
                    cachedTransactionAspect = new TransactionAspect(txManager);
                }
            }
        }
        return cachedTransactionAspect;
    }

    /**
     * 在目标类层级中查找对应接口方法的 {@link Transactional} 注解方法。
     *
     * <p>从 {@code targetClass} 开始向上遍历类层级，直到找到带有 {@link Transactional}
     * 注解的同名同参数方法，或到达 {@link Object} 为止。
     *
     * @param interfaceMethod 接口上声明的方法
     * @return 目标类层级中带有 {@link Transactional} 的方法；若不存在则返回 null
     */
    private Method resolveAnnotatedMethod(Method interfaceMethod) {
        Class<?> current = targetClass;
        while (current != null && current != Object.class) {
            try {
                Method m = current.getDeclaredMethod(interfaceMethod.getName(), interfaceMethod.getParameterTypes());
                if (m.isAnnotationPresent(Transactional.class)) {
                    return m;
                }
            } catch (NoSuchMethodException e) {
                // method not declared in this class, try superclass
            }
            current = current.getSuperclass();
        }
        // Fallback: check the interface method itself
        return interfaceMethod.isAnnotationPresent(Transactional.class) ? interfaceMethod : null;
    }
}
