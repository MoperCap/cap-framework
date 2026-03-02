package org.moper.cap.aop.proxy;

import java.lang.reflect.Method;
import java.util.List;

class AdvisorInvoker {

    static void invokeBefore(List<Advisor> advisors, Method method, Object[] args) throws Exception {
        for (Advisor adv : advisors) {
            if (adv.getType() == Advisor.Type.BEFORE && adv.matches(method)) {
                invokeAdvice(adv.getAdviceMethod(), adv.getAspectInstance(), args);
            }
        }
    }

    static boolean invokeAround(List<Advisor> advisors, Method method, Object[] args) throws Exception {
        boolean aroundInvoked = false;
        for (Advisor adv : advisors) {
            if (adv.getType() == Advisor.Type.AROUND && adv.matches(method)) {
                invokeAdvice(adv.getAdviceMethod(), adv.getAspectInstance(), args);
                aroundInvoked = true;
            }
        }
        return aroundInvoked;
    }

    static void invokeAfter(List<Advisor> advisors, Method method, Object[] args) throws Exception {
        for (Advisor adv : advisors) {
            if (adv.getType() == Advisor.Type.AFTER && adv.matches(method)) {
                invokeAdvice(adv.getAdviceMethod(), adv.getAspectInstance(), args);
            }
        }
    }

    /**
     * 调用通知方法。若通知方法不接受参数（无参），则忽略目标方法的参数直接调用；
     * 否则将目标方法的参数传递给通知方法。
     */
    private static void invokeAdvice(Method adviceMethod, Object aspectInstance, Object[] args) throws Exception {
        if (adviceMethod.getParameterCount() == 0) {
            adviceMethod.invoke(aspectInstance);
        } else {
            adviceMethod.invoke(aspectInstance, args);
        }
    }
}
