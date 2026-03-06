package org.moper.cap.aop.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Default implementation of {@link ProceedingJoinPoint} used by {@code @Around} advice.
 *
 * <p>Extends {@link DefaultJoinPoint} and provides {@link #proceed()} to invoke the
 * original target method.
 *
 * <p>The inherited {@link #args} field may be read via {@link #getArgs()}.
 * Callers wishing to invoke the original method with different arguments should use
 * {@link #proceed(Object[])} rather than mutating the array returned by {@link #getArgs()}.
 */
public class DefaultProceedingJoinPoint extends DefaultJoinPoint implements ProceedingJoinPoint {

    public DefaultProceedingJoinPoint(Object target, Method method, Object[] args) {
        super(target, method, args);
    }

    @Override
    public Object proceed() throws Throwable {
        return proceed(args);
    }

    @Override
    public Object proceed(Object[] newArgs) throws Throwable {
        try {
            method.setAccessible(true);
            return method.invoke(target, newArgs);
        } catch (InvocationTargetException ite) {
            throw ite.getCause() != null ? ite.getCause() : ite;
        }
    }
}
