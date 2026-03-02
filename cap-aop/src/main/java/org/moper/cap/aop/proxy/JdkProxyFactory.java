package org.moper.cap.aop.proxy;

import org.moper.cap.aop.advisor.Advisor;

import java.lang.reflect.Proxy;
import java.util.List;

public class JdkProxyFactory implements ProxyFactory {

    @Override
    public boolean canProxy(Class<?> targetClass) {
        return targetClass.getInterfaces().length > 0;
    }

    @Override
    public Object createProxy(Class<?> targetClass, Object target, List<Advisor> advisors) {
        return Proxy.newProxyInstance(
                targetClass.getClassLoader(),
                targetClass.getInterfaces(),
                (proxy, method, args) -> {
                    AdvisorInvoker.invokeBefore(advisors, method, args);
                    boolean aroundInvoked = AdvisorInvoker.invokeAround(advisors, method, args);
                    Object result = null;
                    if (!aroundInvoked) {
                        method.setAccessible(true);
                        result = method.invoke(target, args);
                    }
                    AdvisorInvoker.invokeAfter(advisors, method, args);
                    return result;
                }
        );
    }
}
