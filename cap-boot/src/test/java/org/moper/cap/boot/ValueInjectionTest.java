package org.moper.cap.boot;

import org.junit.jupiter.api.*;
import org.moper.cap.annotation.Component;
import org.moper.cap.annotation.ComponentScan;
import org.moper.cap.boot.annotation.*;
import org.moper.cap.boot.context.DefaultBootstrapContext;
import org.moper.cap.boot.context.DefaultApplicationContextFactory;
import org.moper.cap.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

class ValueInjectionTest {

    @ComponentScan("org.moper.cap.boot")
    public static class Config {}

    @Component
    public static class DemoComponent {
        @Value("${custom.prop:default}")
        String value;

        @Value("${non.exist:42}")
        int number;
    }

    @Test
    void testValueInjection() {
        try (ApplicationContext ctx =
                     new DefaultBootstrapContext(Config.class).build(DefaultApplicationContextFactory.INSTANCE)) {
            ctx.run();
            DemoComponent bean = ctx.getBean(DemoComponent.class);
            // 依赖 application.yml 存在 custom.prop: hello
            assertEquals("hello", bean.value); // 如果自定义属性不存在，expect "default"
            assertEquals(42, bean.number);     // 随实际配置
        }
    }
}