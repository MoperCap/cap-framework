package org.moper.cap.aop.proxy;

import java.lang.reflect.Method;
import java.util.List;

class AdvisorInvoker {

    static void invokeBefore(List<Advisor> advisors, Method method, Object[] args) throws Exception {
        for (Advisor adv : advisors) {
            if (adv.getType() == Advisor.Type.BEFORE && adv.matches(method)) {
                adv.getAdviceMethod().invoke(adv.getAspectInstance(), args);
            }
        }
    }

    static boolean invokeAround(List<Advisor> advisors, Method method, Object[] args) throws Exception {
        boolean aroundInvoked = false;
        for (Advisor adv : advisors) {
            if (adv.getType() == Advisor.Type.AROUND && adv.matches(method)) {
                adv.getAdviceMethod().invoke(adv.getAspectInstance(), args);
                aroundInvoked = true;
            }
        }
        return aroundInvoked;
    }

    static void invokeAfter(List<Advisor> advisors, Method method, Object[] args) throws Exception {
        for (Advisor adv : advisors) {
            if (adv.getType() == Advisor.Type.AFTER && adv.matches(method)) {
                adv.getAdviceMethod().invoke(adv.getAspectInstance(), args);
            }
        }
    }
}
