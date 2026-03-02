package org.moper.cap.aop.proxy;

import org.moper.cap.aop.advisor.Advisor;

import java.util.List;

public class ProxyResolver {

    private static final JdkProxyFactory JDK_PROXY_FACTORY = new JdkProxyFactory();
    private static final CglibProxyFactory CGLIB_PROXY_FACTORY = new CglibProxyFactory();

    public Object resolve(Object target, List<Advisor> advisors) {
        Class<?> targetClass = target.getClass();
        ProxyFactory factory = JDK_PROXY_FACTORY.canProxy(targetClass)
                ? JDK_PROXY_FACTORY
                : CGLIB_PROXY_FACTORY;
        return factory.createProxy(targetClass, target, advisors);
    }
}
