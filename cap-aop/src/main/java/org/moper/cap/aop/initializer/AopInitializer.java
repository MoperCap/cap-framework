package org.moper.cap.aop.initializer;

import org.moper.cap.aop.annotation.After;
import org.moper.cap.aop.annotation.Around;
import org.moper.cap.aop.annotation.Aspect;
import org.moper.cap.aop.annotation.Before;
import org.moper.cap.context.initializer.Initializer;
import org.moper.cap.context.runner.RunnerType;
import org.moper.cap.context.context.BootstrapContext;
import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.aop.advisor.Advisor;
import org.moper.cap.aop.weaving.AopBeanInterceptor;

import java.lang.reflect.Method;
import java.util.*;

public class AopInitializer extends Initializer {

    public AopInitializer() {
        super(RunnerType.FEATURE, 100, "CapAopInitializer", "Scans and installs AOP Advisors and Interceptors");
    }

    @Override
    public void initialize(BootstrapContext context) throws Exception {
        BeanContainer beanContainer = context.getBeanContainer();
        List<Advisor> advisors = scanAdvisors(beanContainer);
        beanContainer.addBeanInterceptor(new AopBeanInterceptor(advisors));
    }

    private List<Advisor> scanAdvisors(BeanContainer container) {
        List<Advisor> list = new ArrayList<>();

        container.getBeansWithAnnotation(Aspect.class).forEach((key, bean) -> {
            Class<?> clazz = bean.getClass();
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.isAnnotationPresent(Before.class)) {
                    list.add(new Advisor(Advisor.Type.BEFORE, m.getAnnotation(Before.class).value(), bean, m));
                }
                if (m.isAnnotationPresent(Around.class)) {
                    list.add(new Advisor(Advisor.Type.AROUND, m.getAnnotation(Around.class).value(), bean, m));
                }
                if (m.isAnnotationPresent(After.class)) {
                    list.add(new Advisor(Advisor.Type.AFTER, m.getAnnotation(After.class).value(), bean, m));
                }
            }

        });
        return list;
    }
}