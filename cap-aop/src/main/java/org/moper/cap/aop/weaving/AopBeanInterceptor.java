package org.moper.cap.aop.weaving;

import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.exception.BeanException;
import org.moper.cap.bean.interceptor.BeanInterceptor;
import org.moper.cap.aop.advisor.Advisor;
import org.moper.cap.aop.proxy.ProxyResolver;

import java.util.*;

public class AopBeanInterceptor implements BeanInterceptor {

    private final List<Advisor> advisors;
    private final ProxyResolver proxyResolver = new ProxyResolver();

    public AopBeanInterceptor(List<Advisor> advisors) {
        this.advisors = advisors;
    }

    @Override
    public Object afterPropertyInjection(Object bean, BeanDefinition definition) throws BeanException {
        return proxyResolver.resolve(bean, advisors);
    }
}