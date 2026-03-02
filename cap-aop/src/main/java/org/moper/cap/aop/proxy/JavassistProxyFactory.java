package org.moper.cap.aop.proxy;

import javassist.util.proxy.Proxy;

import org.moper.cap.aop.exception.AspectException;

import java.util.List;

/**
 * 基于 Javassist 实现的代理工厂，兼容 Java 17+
 */
public final class JavassistProxyFactory implements ProxyFactory {

    @Override
    public boolean canProxy(Class<?> targetClass) {
        return targetClass.getInterfaces().length == 0;
    }

    @Override
    public Object createProxy(Class<?> targetClass, Object target, List<Advisor> advisors) {
        try {
            javassist.util.proxy.ProxyFactory factory = new javassist.util.proxy.ProxyFactory();
            factory.setSuperclass(targetClass);
            Class<?> proxyClass = factory.createClass();
            Object proxy = proxyClass.getDeclaredConstructor().newInstance();
            ((Proxy) proxy).setHandler((self, method, proceed, args) -> {
                AdvisorInvoker.invokeBefore(advisors, method, args);
                boolean aroundInvoked = AdvisorInvoker.invokeAround(advisors, method, args);
                Object result = null;
                if (!aroundInvoked) {
                    method.setAccessible(true);
                    result = method.invoke(target, args);
                }
                AdvisorInvoker.invokeAfter(advisors, method, args);
                return result;
            });
            return proxy;
        } catch (Exception e) {
            throw new AspectException("Failed to create Javassist proxy for class: " + targetClass.getName(), e);
        }
    }
}
