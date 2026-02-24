package org.moper.cap.example;

import org.junit.jupiter.api.Test;
import org.moper.cap.boot.context.DefaultBootstrapContext;
import org.moper.cap.boot.context.DefaultApplicationContextFactory;
import org.moper.cap.context.context.ApplicationContext;
import org.moper.cap.example.bean.ServiceDemo;
import org.moper.cap.example.boot.AutowiredBeanDemo;

import static org.junit.jupiter.api.Assertions.*;

public class ExampleIntegrationTest {

    @Test
    void testFullFrameworkIntegration() {
        try(ApplicationContext ctx = new DefaultBootstrapContext(org.moper.cap.example.context.ExampleConfig.class)
                                        .build(DefaultApplicationContextFactory.INSTANCE)) {
            ctx.run();

            ServiceDemo demo = ctx.getBean(ServiceDemo.class);
            assertEquals("Hello, World!", demo.greet("World"));

            AutowiredBeanDemo autoBean = ctx.getBean(AutowiredBeanDemo.class);
            assertNotNull(autoBean.serviceDemo);
        }
    }
}