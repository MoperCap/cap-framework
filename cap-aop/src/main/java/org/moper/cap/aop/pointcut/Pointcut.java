package org.moper.cap.aop.pointcut;

import java.lang.reflect.Method;

/**
 * Determines whether a given method on a target class should be intercepted by an advisor.
 */
public interface Pointcut {

    /**
     * Returns {@code true} if this pointcut matches the given method on the target class.
     *
     * @param method      the method to check (may be an interface or proxy method)
     * @param targetClass the actual implementation class (may be {@code null})
     */
    boolean matches(Method method, Class<?> targetClass);
}
