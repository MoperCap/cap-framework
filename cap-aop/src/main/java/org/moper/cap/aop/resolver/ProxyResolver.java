package org.moper.cap.aop.resolver;

import org.moper.cap.aop.proxy.Advisor;
import org.moper.cap.aop.proxy.JavassistProxyFactory;
import org.moper.cap.aop.proxy.JdkProxyFactory;

import java.util.List;

public class ProxyResolver {

    private static final JdkProxyFactory JDK_PROXY_FACTORY = new JdkProxyFactory();
    private static final JavassistProxyFactory JAVASSIST_PROXY_FACTORY = new JavassistProxyFactory();

    public Object resolve(Object target, List<Advisor> advisors) {
        Class<?> targetClass = target.getClass();

        if(JDK_PROXY_FACTORY.canProxy(targetClass)) {
            return JDK_PROXY_FACTORY.createProxy(targetClass, target, advisors);
        } else if(JAVASSIST_PROXY_FACTORY.canProxy(targetClass)) {
            return JAVASSIST_PROXY_FACTORY.createProxy(targetClass, target, advisors);
        } else throw new AssertionError("ProxyResolver does not support " + target.getClass());
    }
}
