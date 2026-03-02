package org.moper.cap.aop.proxy;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import org.moper.cap.aop.exception.AspectException;

import java.util.List;

/**
 * 基于 Byte Buddy 实现的代理工厂，兼容 Java 17+
 */
public final class ByteBuddyProxyFactory implements ProxyFactory {

    @Override
    public boolean canProxy(Class<?> targetClass) {
        return targetClass.getInterfaces().length == 0;
    }

    @Override
    public Object createProxy(Class<?> targetClass, Object target, List<Advisor> advisors) {
        try {
            Class<?> proxyClass = new ByteBuddy()
                    .subclass(targetClass)
                    .method(ElementMatchers.any())
                    .intercept(MethodDelegation.to(new AopMethodInterceptor(target, advisors)))
                    .make()
                    .load(targetClass.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                    .getLoaded();
            return proxyClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new AspectException("Failed to create ByteBuddy proxy for class: " + targetClass.getName(), e);
        }
    }
}
