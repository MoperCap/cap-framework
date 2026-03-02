package org.moper.cap.aop.proxy;

import java.util.List;

public sealed interface ProxyFactory permits JdkProxyFactory, ByteBuddyProxyFactory{
    Object createProxy(Class<?> targetClass, Object target, List<Advisor> advisors);
    boolean canProxy(Class<?> targetClass);
}
