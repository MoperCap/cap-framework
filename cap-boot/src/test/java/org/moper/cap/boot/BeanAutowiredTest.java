package org.moper.cap.boot;

import org.junit.jupiter.api.*;
import org.moper.cap.context.annotation.Component;
import org.moper.cap.context.annotation.ComponentScan;
import org.moper.cap.boot.annotation.*;
import org.moper.cap.boot.context.DefaultBootstrapContext;
import org.moper.cap.boot.context.DefaultApplicationContextFactory;
import org.moper.cap.context.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

class BeanAutowiredTest {

    @ComponentScan("org.moper.cap.boot")
    public static class Config {}

    @Component
    public static class ServiceA {
        @Autowired
        ServiceB serviceB;
    }

    @Component
    public static class ServiceB {}

    @Test
    void testAutowiredInjection() {
        try (ApplicationContext ctx =
                     new DefaultBootstrapContext(Config.class).build(DefaultApplicationContextFactory.INSTANCE)) {
            ctx.run();
            ServiceA a = ctx.getBean(ServiceA.class);
            assertNotNull(a);
            assertNotNull(a.serviceB);
        }
    }
}