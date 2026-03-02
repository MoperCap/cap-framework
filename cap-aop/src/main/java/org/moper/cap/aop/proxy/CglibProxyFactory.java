package org.moper.cap.aop.proxy;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 基于Cglib实现的代理工厂
 *
 * @deprecated 由于 Cglib 对 Java17+ 的支持并不理想，因此此代理工厂并不能正常运行
 */
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
                // 使用 MethodProxy 而不是 Method.invoke
                result = proxy.invokeSuper(obj, args);
            }
            AdvisorInvoker.invokeAfter(advisors, method, args);
            return result;
        });
        return enhancer.create();
    }
}
