package org.moper.cap.aop.proxy;

import org.moper.cap.aop.advisor.Advisor;

import java.util.List;

public interface ProxyFactory {
    Object createProxy(Class<?> targetClass, Object target, List<Advisor> advisors);
    boolean canProxy(Class<?> targetClass);
}
