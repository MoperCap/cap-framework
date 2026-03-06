package org.moper.cap.aop.model;

import java.lang.reflect.Method;

/**
 * Provides reflective access to the signature of an intercepted method.
 */
public interface MethodSignature {

    /**
     * Returns the intercepted method.
     */
    Method getMethod();

    /**
     * Returns the simple name of the intercepted method.
     */
    String getName();

    /**
     * Returns the return type of the intercepted method.
     */
    Class<?> getReturnType();

    /**
     * Returns the parameter types of the intercepted method.
     */
    Class<?>[] getParameterTypes();

    /**
     * Returns the class that declares the intercepted method.
     */
    Class<?> getDeclaringType();
}
