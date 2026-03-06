package org.moper.cap.aop.interceptor;

import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.exception.BeanException;
import org.moper.cap.bean.interceptor.BeanInterceptor;
import org.moper.cap.aop.proxy.Advisor;
import org.moper.cap.aop.resolver.ProxyResolver;

import java.lang.reflect.Method;
import java.util.*;

public class AopBeanInterceptor implements BeanInterceptor {

    private final List<Advisor> advisors;
    private final ProxyResolver proxyResolver = new ProxyResolver();

    public AopBeanInterceptor(List<Advisor> advisors) {
        this.advisors = advisors;
    }

    @Override
    public Object afterPropertyInjection(Object bean, BeanDefinition definition) throws BeanException {
        if (advisors.isEmpty() || !hasMatchingAdvisor(bean)) {
            return bean;
        }
        return proxyResolver.resolve(bean, advisors);
    }

    /**
     * 检查当前 Bean 是否有任何匹配的 Advisor。
     *
     * <p>同时检查 Bean 类本身的方法以及其所有接口中的方法，
     * 以正确支持 JDK Proxy（接口方法）和 CGLib Proxy（类方法）两种代理模式。
     * The bean's actual class is always passed as {@code targetClass} so that
     * annotation-based pointcuts (e.g. {@code @method(...)}) can resolve
     * annotations from the implementation, not just the interface.
     */
    private boolean hasMatchingAdvisor(Object bean) {
        Class<?> beanClass = bean.getClass();
        // 检查类本身声明的方法
        for (Method m : beanClass.getDeclaredMethods()) {
            for (Advisor advisor : advisors) {
                if (advisor.matches(m, beanClass)) return true;
            }
        }
        // 检查接口方法（JDK Proxy 场景）
        for (Class<?> iface : beanClass.getInterfaces()) {
            for (Method m : iface.getDeclaredMethods()) {
                for (Advisor advisor : advisors) {
                    if (advisor.matches(m, beanClass)) return true;
                }
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 400;
    }
}