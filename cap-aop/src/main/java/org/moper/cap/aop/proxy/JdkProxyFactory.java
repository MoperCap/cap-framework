package org.moper.cap.aop.proxy;

import org.moper.cap.aop.model.DefaultProceedingJoinPoint;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * 基于JDK实现的代理工厂
 */
public final class JdkProxyFactory implements ProxyFactory {

    @Override
    public boolean canProxy(Class<?> targetClass) {
        return targetClass.getInterfaces().length > 0;
    }

    @Override
    public Object createProxy(Class<?> targetClass, Object target, List<Advisor> advisors) {
        return Proxy.newProxyInstance(
                targetClass.getClassLoader(),
                targetClass.getInterfaces(),
                (proxy, method, args) -> invoke(target, method, args, advisors)
        );
    }

    static Object invoke(Object target, Method method, Object[] args,
                         List<Advisor> advisors) throws Throwable {
        Class<?> actualTargetClass = target.getClass();
        DefaultProceedingJoinPoint pjp = new DefaultProceedingJoinPoint(target, method, args);

        AdvisorInvoker.invokeBefore(advisors, method, actualTargetClass, pjp);

        Throwable thrown = null;
        Object result = null;
        try {
            result = AdvisorInvoker.invokeAround(advisors, method, actualTargetClass, pjp);
        } catch (Throwable t) {
            thrown = t;
        } finally {
            AdvisorInvoker.invokeAfter(advisors, method, actualTargetClass, pjp);
        }

        if (thrown != null) {
            AdvisorInvoker.invokeAfterThrowing(advisors, method, actualTargetClass, pjp, thrown);
            throw thrown;
        }
        return result;
    }
}

