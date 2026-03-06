package org.moper.cap.aop.proxy;

import org.moper.cap.aop.model.JoinPoint;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

class AdvisorInvoker {

    static void invokeBefore(List<Advisor> advisors, Method method, Class<?> targetClass, Object[] args)
            throws Exception {
        for (Advisor adv : advisors) {
            if (adv.getType() == Advisor.Type.BEFORE && adv.matches(method, targetClass)) {
                invokeSimpleAdvice(adv.getAdviceMethod(), adv.getAspectInstance(), args);
            }
        }
    }

    /**
     * Invokes the first matching {@code @Around} advisor, passing a {@link JoinPoint} that
     * delegates {@link JoinPoint#proceed()} to the original method. If no matching around
     * advisor is found, calls {@link JoinPoint#proceed()} directly.
     *
     * @return the return value from the around advice or from the original method
     */
    static Object invokeAround(List<Advisor> advisors, Method method, Class<?> targetClass,
                               JoinPoint joinPoint) throws Throwable {
        for (Advisor adv : advisors) {
            if (adv.getType() == Advisor.Type.AROUND && adv.matches(method, targetClass)) {
                return invokeAroundAdvice(adv.getAdviceMethod(), adv.getAspectInstance(), joinPoint);
            }
        }
        // No around advisor - proceed with the original method
        return joinPoint.proceed();
    }

    static void invokeAfter(List<Advisor> advisors, Method method, Class<?> targetClass, Object[] args)
            throws Exception {
        for (Advisor adv : advisors) {
            if (adv.getType() == Advisor.Type.AFTER && adv.matches(method, targetClass)) {
                invokeSimpleAdvice(adv.getAdviceMethod(), adv.getAspectInstance(), args);
            }
        }
    }

    /**
     * Invokes a {@code @Before} / {@code @After} advice method.
     * If the advice accepts no parameters the target method's arguments are ignored.
     */
    private static void invokeSimpleAdvice(Method adviceMethod, Object aspectInstance, Object[] args)
            throws Exception {
        try {
            if (adviceMethod.getParameterCount() == 0) {
                adviceMethod.invoke(aspectInstance);
            } else {
                adviceMethod.invoke(aspectInstance, args);
            }
        } catch (InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            if (cause instanceof Exception e) throw e;
            throw new RuntimeException(cause);
        }
    }

    /**
     * Invokes an {@code @Around} advice method.
     * If the advice accepts a {@link JoinPoint} parameter it is passed; otherwise the advice
     * is called with no arguments and {@link JoinPoint#proceed()} is called automatically.
     */
    private static Object invokeAroundAdvice(Method adviceMethod, Object aspectInstance,
                                             JoinPoint joinPoint) throws Throwable {
        try {
            if (adviceMethod.getParameterCount() > 0
                    && JoinPoint.class.isAssignableFrom(adviceMethod.getParameterTypes()[0])) {
                return adviceMethod.invoke(aspectInstance, joinPoint);
            } else {
                adviceMethod.invoke(aspectInstance);
                return null;
            }
        } catch (InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            throw cause != null ? cause : ite;
        }
    }
}

