package org.moper.cap.aop.proxy;

import lombok.Getter;
import org.moper.cap.aop.pointcut.Pointcut;
import org.moper.cap.aop.pointcut.PointcutParser;

import java.lang.reflect.Method;

public class Advisor {

    public enum Type { BEFORE, AFTER, AROUND, AFTER_THROWING }

    @Getter
    private final Type type;
    private final Pointcut pointcut;
    @Getter
    private final Object aspectInstance;
    @Getter
    private final Method adviceMethod;
    /**
     * The expected exception type for {@link Type#AFTER_THROWING} advisors.
     * {@code null} means the advisor matches any throwable.
     */
    @Getter
    private final Class<?> exceptionType;

    /**
     * Primary constructor.
     *
     * @param type           the advice type
     * @param pointcut       the pointcut that determines which methods are intercepted
     * @param aspectInstance the aspect bean instance
     * @param adviceMethod   the advice method to invoke
     * @param exceptionType  the exception type to match (only used for {@link Type#AFTER_THROWING});
     *                       {@code null} means match any throwable
     */
    public Advisor(Type type, Pointcut pointcut, Object aspectInstance, Method adviceMethod,
                   Class<?> exceptionType) {
        this.type = type;
        this.pointcut = pointcut;
        this.aspectInstance = aspectInstance;
        this.adviceMethod = adviceMethod;
        this.exceptionType = exceptionType;
    }

    /**
     * Convenience constructor that parses a pointcut expression string.
     *
     * <p>The exception type is derived automatically from the advice method parameters for
     * {@link Type#AFTER_THROWING} advisors.
     *
     * @param type                the advice type
     * @param pointcutExpression  the pointcut expression string (parsed via {@link PointcutParser})
     * @param aspectInstance      the aspect bean instance
     * @param adviceMethod        the advice method to invoke
     */
    public Advisor(Type type, String pointcutExpression, Object aspectInstance, Method adviceMethod) {
        this(type, PointcutParser.parse(pointcutExpression), aspectInstance, adviceMethod,
                deriveExceptionType(adviceMethod, type));
    }

    /**
     * Returns {@code true} if this advisor matches the given method on the target class.
     *
     * @param m           the method to match (may be an interface or proxy method)
     * @param targetClass the actual implementation class (may be {@code null})
     */
    public boolean matches(Method m, Class<?> targetClass) {
        return pointcut.matches(m, targetClass);
    }

    /**
     * Returns {@code true} if this advisor matches the given method.
     */
    public boolean matches(Method m) {
        return pointcut.matches(m, null);
    }

    /**
     * Derives the exception type for an {@link Type#AFTER_THROWING} advisor by inspecting the
     * advice method's parameter list for the first {@link Throwable} subtype.
     */
    private static Class<?> deriveExceptionType(Method adviceMethod, Type type) {
        if (type != Type.AFTER_THROWING) return null;
        for (Class<?> paramType : adviceMethod.getParameterTypes()) {
            if (Throwable.class.isAssignableFrom(paramType)) {
                return paramType;
            }
        }
        return null; // matches all throwables
    }
}