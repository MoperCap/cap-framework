package org.moper.cap.aop.model;

import java.lang.reflect.Method;

/**
 * Default implementation of {@link JoinPoint} that captures the method execution context.
 *
 * <p>Used by {@code @Before}, {@code @After}, and {@code @AfterThrowing} advice.
 * For {@code @Around} advice use {@link DefaultProceedingJoinPoint}.
 */
public class DefaultJoinPoint implements JoinPoint {

    protected final Object target;
    protected final Method method;
    protected Object[] args;
    private final MethodSignature signature;

    public DefaultJoinPoint(Object target, Method method, Object[] args) {
        this.target = target;
        this.method = method;
        this.args = args;
        this.signature = new MethodSignatureImpl(method);
    }

    @Override
    public Object getTarget() {
        return target;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object[] getArgs() {
        return args;
    }

    @Override
    public MethodSignature getSignature() {
        return signature;
    }
}
