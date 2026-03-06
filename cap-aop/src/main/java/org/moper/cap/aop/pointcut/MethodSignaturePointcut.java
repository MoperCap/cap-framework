package org.moper.cap.aop.pointcut;

import java.lang.reflect.Method;

/**
 * A {@link Pointcut} that matches methods by their fully-qualified signature.
 *
 * <p>The signature format is {@code "fully.qualified.ClassName.methodName"}.
 * The match is exact: both the class name and method name must match.
 */
public class MethodSignaturePointcut implements Pointcut {

    private final String signature;

    /**
     * @param signature the pointcut expression in the form {@code "className.methodName"}
     */
    public MethodSignaturePointcut(String signature) {
        this.signature = signature;
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        String sig = method.getDeclaringClass().getName() + "." + method.getName();
        return sig.equals(signature);
    }

    @Override
    public String toString() {
        return "MethodSignaturePointcut[" + signature + "]";
    }
}
