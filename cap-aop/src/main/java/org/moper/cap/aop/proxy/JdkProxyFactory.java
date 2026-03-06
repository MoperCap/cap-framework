package org.moper.cap.aop.proxy;

import org.moper.cap.aop.model.JoinPoint;

import java.lang.reflect.InvocationTargetException;
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
                (proxy, method, args) -> {
                    Class<?> actualTargetClass = target.getClass();
                    AdvisorInvoker.invokeBefore(advisors, method, actualTargetClass, args);

                    JoinPoint joinPoint = new DefaultJoinPoint(target, method, args);
                    Object result = AdvisorInvoker.invokeAround(advisors, method, actualTargetClass, joinPoint);

                    AdvisorInvoker.invokeAfter(advisors, method, actualTargetClass, args);
                    return result;
                }
        );
    }

    /** Default JoinPoint implementation used inside JDK proxies. */
    static final class DefaultJoinPoint implements JoinPoint {
        private final Object target;
        private final Method method;
        private final Object[] args;

        DefaultJoinPoint(Object target, Method method, Object[] args) {
            this.target = target;
            this.method = method;
            this.args = args;
        }

        @Override public Object getTarget() { return target; }
        @Override public Method getMethod() { return method; }
        @Override public Object[] getArgs()  { return args; }

        @Override
        public Object proceed() throws Throwable {
            try {
                method.setAccessible(true);
                return method.invoke(target, args);
            } catch (InvocationTargetException ite) {
                throw ite.getCause() != null ? ite.getCause() : ite;
            }
        }
    }
}

