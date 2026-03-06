package org.moper.cap.aop.proxy;

import org.moper.cap.aop.model.JoinPoint;
import org.moper.cap.aop.model.ProceedingJoinPoint;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

class AdvisorInvoker {

    static void invokeBefore(List<Advisor> advisors, Method method, Class<?> targetClass,
                             JoinPoint joinPoint) throws Exception {
        Object[] originalArgs = joinPoint.getArgs();
        for (Advisor adv : advisors) {
            if (adv.getType() == Advisor.Type.BEFORE && adv.matches(method, targetClass)) {
                invokeSimpleAdvice(adv.getAdviceMethod(), adv.getAspectInstance(), joinPoint, originalArgs);
            }
        }
    }

    /**
     * Invokes the first matching {@code @Around} advisor, passing a {@link ProceedingJoinPoint}.
     * If no matching around advisor is found, calls {@link ProceedingJoinPoint#proceed()} directly.
     *
     * @return the return value from the around advice or from the original method
     */
    static Object invokeAround(List<Advisor> advisors, Method method, Class<?> targetClass,
                               ProceedingJoinPoint pjp) throws Throwable {
        for (Advisor adv : advisors) {
            if (adv.getType() == Advisor.Type.AROUND && adv.matches(method, targetClass)) {
                return invokeAroundAdvice(adv.getAdviceMethod(), adv.getAspectInstance(), pjp);
            }
        }
        // No around advisor - proceed with the original method
        return pjp.proceed();
    }

    static void invokeAfter(List<Advisor> advisors, Method method, Class<?> targetClass,
                            JoinPoint joinPoint) throws Exception {
        Object[] originalArgs = joinPoint.getArgs();
        for (Advisor adv : advisors) {
            if (adv.getType() == Advisor.Type.AFTER && adv.matches(method, targetClass)) {
                invokeSimpleAdvice(adv.getAdviceMethod(), adv.getAspectInstance(), joinPoint, originalArgs);
            }
        }
    }

    /**
     * Invokes all matching {@code @AfterThrowing} advisors for the given exception.
     * An advisor is skipped if its declared exception type is not assignable from the actual exception.
     */
    static void invokeAfterThrowing(List<Advisor> advisors, Method method, Class<?> targetClass,
                                    JoinPoint joinPoint, Throwable thrown) throws Exception {
        for (Advisor adv : advisors) {
            if (adv.getType() == Advisor.Type.AFTER_THROWING && adv.matches(method, targetClass)) {
                Class<?> expectedType = adv.getExceptionType();
                if (expectedType == null || expectedType.isInstance(thrown)) {
                    invokeAfterThrowingAdvice(adv.getAdviceMethod(), adv.getAspectInstance(),
                            joinPoint, thrown);
                }
            }
        }
    }

    /**
     * Invokes a {@code @Before} / {@code @After} advice method.
     *
     * <p>Parameters are injected by type:
     * <ul>
     *   <li>{@link JoinPoint}: the current join point</li>
     *   <li>No parameters: called with no arguments</li>
     *   <li>Other parameter types: the original method arguments are passed</li>
     * </ul>
     */
    private static void invokeSimpleAdvice(Method adviceMethod, Object aspectInstance,
                                           JoinPoint joinPoint, Object[] originalArgs) throws Exception {
        try {
            adviceMethod.setAccessible(true);
            Object[] args = buildArgs(adviceMethod, joinPoint, null, originalArgs);
            adviceMethod.invoke(aspectInstance, args);
        } catch (InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            if (cause instanceof Exception e) throw e;
            throw new RuntimeException(cause);
        }
    }

    /**
     * Invokes an {@code @Around} advice method.
     *
     * <p>The advice method receives a {@link ProceedingJoinPoint} (or {@link JoinPoint}) if it
     * declares such a parameter; otherwise it is called with no arguments and {@code proceed()} is
     * invoked automatically.
     */
    private static Object invokeAroundAdvice(Method adviceMethod, Object aspectInstance,
                                             ProceedingJoinPoint pjp) throws Throwable {
        try {
            adviceMethod.setAccessible(true);
            Class<?>[] paramTypes = adviceMethod.getParameterTypes();
            if (paramTypes.length > 0 && JoinPoint.class.isAssignableFrom(paramTypes[0])) {
                return adviceMethod.invoke(aspectInstance, pjp);
            } else {
                adviceMethod.invoke(aspectInstance);
                return pjp.proceed();
            }
        } catch (InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            throw cause != null ? cause : ite;
        }
    }

    /**
     * Invokes an {@code @AfterThrowing} advice method.
     *
     * <p>Parameters are injected by type:
     * <ul>
     *   <li>{@link JoinPoint}: the current join point</li>
     *   <li>{@link Throwable} subtype: the thrown exception</li>
     * </ul>
     */
    private static void invokeAfterThrowingAdvice(Method adviceMethod, Object aspectInstance,
                                                   JoinPoint joinPoint, Throwable thrown) throws Exception {
        try {
            adviceMethod.setAccessible(true);
            Object[] args = buildArgs(adviceMethod, joinPoint, thrown, joinPoint.getArgs());
            adviceMethod.invoke(aspectInstance, args);
        } catch (InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            if (cause instanceof Exception e) throw e;
            throw new RuntimeException(cause);
        }
    }

    /**
     * Builds the argument array for invoking an advice method.
     *
     * <p>For each parameter type in the advice method:
     * <ul>
     *   <li>{@link JoinPoint} or subtype → injects {@code joinPoint}</li>
     *   <li>{@link Throwable} subtype → injects {@code thrown} (may be {@code null})</li>
     *   <li>Anything else → tries to match a value from {@code originalArgs} by assignability;
     *       falls back to {@code null} if none matches</li>
     * </ul>
     */
    private static Object[] buildArgs(Method adviceMethod, JoinPoint joinPoint,
                                      Throwable thrown, Object[] originalArgs) {
        Class<?>[] paramTypes = adviceMethod.getParameterTypes();
        if (paramTypes.length == 0) {
            return new Object[0];
        }

        // Check if any parameter is a JoinPoint or Throwable type
        boolean hasSpecialParam = false;
        for (Class<?> pt : paramTypes) {
            if (JoinPoint.class.isAssignableFrom(pt) || Throwable.class.isAssignableFrom(pt)) {
                hasSpecialParam = true;
                break;
            }
        }

        if (!hasSpecialParam) {
            // Backward-compatible: pass original method args directly
            return originalArgs != null ? originalArgs : new Object[0];
        }

        List<Object> args = new ArrayList<>();
        for (Class<?> pt : paramTypes) {
            if (JoinPoint.class.isAssignableFrom(pt)) {
                args.add(joinPoint);
            } else if (Throwable.class.isAssignableFrom(pt)) {
                args.add(thrown);
            } else {
                // Try to match a value from originalArgs by type assignability
                Object matched = null;
                if (originalArgs != null) {
                    for (Object arg : originalArgs) {
                        if (arg != null && pt.isAssignableFrom(arg.getClass())) {
                            matched = arg;
                            break;
                        }
                    }
                }
                args.add(matched);
            }
        }
        return args.toArray();
    }
}

