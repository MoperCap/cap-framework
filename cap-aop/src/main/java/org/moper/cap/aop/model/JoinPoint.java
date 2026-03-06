package org.moper.cap.aop.model;

import java.lang.reflect.Method;

/**
 * AOP join point - provides context about the method being intercepted.
 *
 * <p>Provides access to the target object, the intercepted method, its arguments,
 * and the method signature. Used by {@code @Before}, {@code @After}, and
 * {@code @AfterThrowing} advice.
 *
 * <p>To proceed with the original method invocation (for {@code @Around} advice),
 * use {@link ProceedingJoinPoint}.
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
     * Returns the signature of the intercepted method.
     */
    MethodSignature getSignature();
}
