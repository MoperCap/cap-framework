package org.moper.cap.transaction.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.transaction.annotation.Transactional;
import org.moper.cap.transaction.aspect.TransactionAspect;

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
 *   <li>若存在，委托 {@link TransactionAspect} 进行事务生命周期管理</li>
 *   <li>若不存在，直接透传到目标对象</li>
 * </ol>
 */
@Slf4j
public class TransactionInvocationHandler implements InvocationHandler {

    private final Object target;
    private final Class<?> targetClass;
    private final TransactionAspect transactionAspect;

    public TransactionInvocationHandler(Object target, Class<?> targetClass, TransactionAspect transactionAspect) {
        this.target = target;
        this.targetClass = targetClass;
        this.transactionAspect = transactionAspect;
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

        try {
            // 1. 事务开始
            transactionAspect.handleTransactionBegin(targetMethod);

            // 2. 调用目标方法
            Object result = method.invoke(target, args);

            // 3. 事务提交（正常流程）
            transactionAspect.handleTransactionEnd(targetMethod, null);

            return result;
        } catch (InvocationTargetException ite) {
            Throwable cause = ite.getCause() != null ? ite.getCause() : ite;
            // 4. 捕获异常，进行事务回滚或提交
            transactionAspect.handleTransactionEnd(targetMethod, cause);
            throw cause;
        } catch (Exception e) {
            transactionAspect.handleTransactionEnd(targetMethod, e);
            throw e;
        }
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
