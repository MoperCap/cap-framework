package org.moper.cap.aop.proxy;

import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import org.moper.cap.aop.exception.AspectException;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 基于 Javassist 实现的代理工厂，通过 __target 引用模式支持无接口类代理
 */
public final class JavassistProxyFactory implements org.moper.cap.aop.proxy.ProxyFactory {

    private static final Unsafe UNSAFE = getUnsafe();

    private static Unsafe getUnsafe() {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            return (Unsafe) unsafeField.get(null);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Override
    public boolean canProxy(Class<?> targetClass) {
        return targetClass.getInterfaces().length == 0;
    }

    @Override
    public Object createProxy(Class<?> targetClass, Object target, List<Advisor> advisors) {
        try {
            ProxyFactory factory = new ProxyFactory();
            factory.setSuperclass(targetClass);
            factory.setFilter(m -> {
                int mod = m.getModifiers();
                return !java.lang.reflect.Modifier.isAbstract(mod)
                        && !java.lang.reflect.Modifier.isNative(mod)
                        && !java.lang.reflect.Modifier.isStatic(mod);
            });

            Class<?> proxyClass = factory.createClass();

            // Allocate instance without invoking any constructor, so classes
            // without a no-arg constructor are also supported.
            Object proxyInstance = UNSAFE.allocateInstance(proxyClass);

            ((Proxy) proxyInstance).setHandler((self, thisMethod, proceed, args) -> {
                AdvisorInvoker.invokeBefore(advisors, thisMethod, args);
                boolean aroundInvoked = AdvisorInvoker.invokeAround(advisors, thisMethod, args);
                Object result = null;
                if (!aroundInvoked) {
                    thisMethod.setAccessible(true);
                    result = thisMethod.invoke(target, args);
                }
                AdvisorInvoker.invokeAfter(advisors, thisMethod, args);
                return result;
            });

            return proxyInstance;
        } catch (Exception e) {
            throw new AspectException("Failed to create Javassist proxy for class: " + targetClass.getName(), e);
        }
    }
}
