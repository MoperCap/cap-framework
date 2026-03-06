package org.moper.cap.aop.model;

import java.lang.reflect.Method;

/**
 * Default implementation of {@link MethodSignature}.
 */
public class MethodSignatureImpl implements MethodSignature {

    private final Method method;

    public MethodSignatureImpl(Method method) {
        this.method = method;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public String getName() {
        return method.getName();
    }

    @Override
    public Class<?> getReturnType() {
        return method.getReturnType();
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return method.getParameterTypes();
    }

    @Override
    public Class<?> getDeclaringType() {
        return method.getDeclaringClass();
    }

    @Override
    public String toString() {
        return method.getDeclaringClass().getName() + "." + method.getName();
    }
}
