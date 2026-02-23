package org.moper.cap.boot;

import org.junit.jupiter.api.*;
import org.moper.cap.boot.annotation.ComponentScan;
import org.moper.cap.boot.annotation.ResourceScan;
import org.moper.cap.boot.context.DefaultBootstrapContext;
import org.moper.cap.boot.context.DefaultApplicationContextFactory;
import org.moper.cap.context.ApplicationContext;
import org.moper.cap.environment.Environment;
import org.moper.cap.property.subscriber.PropertyViewPool;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class BasicConfigLoadTest {

    // 该测试假设 classpath 有一个 application.yml/yaml/properties
    // 包含 custom.prop: 'hello' 和 number: 42

    @ResourceScan
    @ComponentScan("org.moper.cap.boot")
    public static class EmptyConfig {}

    @Test
    void testBootstrapAndReadProperty() {
        try (ApplicationContext ctx =
                     new DefaultBootstrapContext(EmptyConfig.class).build(DefaultApplicationContextFactory.INSTANCE)) {
            ctx.run();
            Environment env = ctx.getEnvironment();
            PropertyViewPool viewPool = env.getViewPool();
            // 读取字符串属性
            assertEquals("hello", viewPool.getPropertyValue("custom.prop", String.class));
            // 读取数字属性
            assertEquals(42, viewPool.getPropertyValue("number", Integer.class));
            // 不存在属性返回 null
            assertNull(viewPool.getPropertyValue("non.exist", String.class));
        }
    }
}