package org.moper.cap.aop.weaving;

import org.moper.cap.aop.advisor.Advisor;
import java.lang.reflect.*;
import java.util.*;

public class BeanProxyFactory {

    private final List<Advisor> advisors;

    public BeanProxyFactory(List<Advisor> advisors) {
        this.advisors = advisors;
    }

    public Object createProxy(Object bean) {
        Class<?> beanClass = bean.getClass();
        Class<?>[] interfaces = beanClass.getInterfaces();
        if (interfaces.length == 0) return bean; // 如果无接口则不代理

        return Proxy.newProxyInstance(
            beanClass.getClassLoader(),
            interfaces,
            (proxy, method, args) -> {
                // before
                for (Advisor adv : advisors) {
                    if (adv.getType() == Advisor.Type.BEFORE && adv.matches(method)) {
                        adv.getAdviceMethod().invoke(adv.getAspectInstance(), args);
                    }
                }
                // around
                boolean aroundInvoked = false;
                for (Advisor adv : advisors) {
                    if (adv.getType() == Advisor.Type.AROUND && adv.matches(method)) {
                        adv.getAdviceMethod().invoke(adv.getAspectInstance(), args);
                        aroundInvoked = true;
                    }
                }
                Object result = null;
                if (!aroundInvoked) {
                    method.setAccessible(true);
                    result = method.invoke(bean, args);
                }
                // after
                for (Advisor adv : advisors) {
                    if (adv.getType() == Advisor.Type.AFTER && adv.matches(method)) {
                        adv.getAdviceMethod().invoke(adv.getAspectInstance(), args);
                    }
                }
                return result;
            }
        );
    }
}