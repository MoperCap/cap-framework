package org.moper.cap.aop;

import org.junit.jupiter.api.Test;
import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.bean.container.impl.DefaultBeanContainer;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.interceptor.BeanInterceptor;
import org.moper.cap.aop.weaving.AopBeanInterceptor;
import org.moper.cap.aop.advisor.Advisor;
import org.moper.cap.aop.annotation.Aspect;
import org.moper.cap.aop.annotation.Before;
import org.moper.cap.aop.annotation.After;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class AopBeanInterceptorTest {

    interface HelloService {
        void sayHello();
    }

    public static class HelloServiceImpl implements HelloService {
        public void sayHello() { System.out.println("Hello!"); }
    }

    @Aspect
    public static class LogAspect {
        public boolean beforeFired = false;
        public boolean afterFired = false;

        @Before("org.moper.cap.aop.AopBeanInterceptorTest$HelloService.sayHello")
        public void beforeHello() { beforeFired = true; }

        @After("org.moper.cap.aop.AopBeanInterceptorTest$HelloService.sayHello")
        public void afterHello() { afterFired = true; }
    }

    @Test
    void testAopAdviceWithBeanContainer() throws Exception {
        // 只依赖cap-bean & cap-aop
        BeanContainer container = new DefaultBeanContainer();
        container.registerBeanDefinition(BeanDefinition.of("helloService", HelloServiceImpl.class));
        container.registerBeanDefinition(BeanDefinition.of("logAspect", LogAspect.class));

        HelloService svc = container.getBean("helloService", HelloService.class);
        LogAspect aspect = container.getBean("logAspect", LogAspect.class);

        // 扫描切面advisor
        List<Advisor> advisors = new ArrayList<>();
        for (Method m : LogAspect.class.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Before.class)) {
                advisors.add(new Advisor(
                        Advisor.Type.BEFORE,
                        m.getAnnotation(Before.class).value(),
                        aspect, m
                ));
            }
            if (m.isAnnotationPresent(After.class)) {
                advisors.add(new Advisor(
                        Advisor.Type.AFTER,
                        m.getAnnotation(After.class).value(),
                        aspect, m
                ));
            }
        }

        // 手动添加Aop拦截器
        BeanInterceptor interceptor = new AopBeanInterceptor(advisors);
        svc = (HelloService) interceptor.afterPropertyInjection(svc, null);
        svc.sayHello();

        assertTrue(aspect.beforeFired);
        assertTrue(aspect.afterFired);
    }
}