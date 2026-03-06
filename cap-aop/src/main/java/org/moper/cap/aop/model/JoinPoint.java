package org.moper.cap.aop.model;

import java.lang.reflect.Method;

/**
 * AOP join point - represents the method being intercepted by an {@code @Around} advice.
 *
 * <p>Provides access to the target object, the intercepted method, and its arguments,
 * and allows the advice to invoke the original method via {@link #proceed()}.
 */
public interface JoinPoint {

    /**
     * Returns the target object on which the intercepted method is being called.
     */
    Object getTarget();

    /**
     * Returns the intercepted method.
     */
    Method getMethod();

    /**
     * Returns the arguments passed to the intercepted method.
     */
    Object[] getArgs();

    /**
     * Proceeds with the original method invocation.
     *
     * @return the return value of the original method
     * @throws Throwable if the original method throws
     */
    Object proceed() throws Throwable;
}
