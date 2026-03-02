package org.moper.cap.aop.proxy;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import java.util.List;

public final class CglibProxyFactory implements ProxyFactory {

    @Override
    public boolean canProxy(Class<?> targetClass) {
        return targetClass.getInterfaces().length == 0;
    }

    @Override
    public Object createProxy(Class<?> targetClass, Object target, List<Advisor> advisors) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(targetClass);
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
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
        return enhancer.create();
    }
}
