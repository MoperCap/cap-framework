package org.moper.cap.aop.proxy;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import java.lang.reflect.Method;
import java.util.List;

/**
 * ByteBuddy 方法拦截器，实现 AOP 通知逻辑
 */
class AopMethodInterceptor {

    private final Object target;
    private final List<Advisor> advisors;

    AopMethodInterceptor(Object target, List<Advisor> advisors) {
        this.target = target;
        this.advisors = advisors;
    }

    @RuntimeType
    public Object intercept(@Origin Method method, @AllArguments Object[] args) throws Exception {
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
}
