package org.moper.cap.boot;

import org.junit.jupiter.api.Test;
import org.moper.cap.boot.annotation.Autowired;
import org.moper.cap.boot.context.DefaultApplicationContextFactory;
import org.moper.cap.boot.context.DefaultBootstrapContext;
import org.moper.cap.context.annotation.Bean;
import org.moper.cap.context.annotation.Component;
import org.moper.cap.context.annotation.ComponentScan;
import org.moper.cap.context.annotation.Configuration;
import org.moper.cap.context.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

public class BeanAutowiredTest {

    @ComponentScan("org.moper.cap.boot")
    public static class Config {}

    @Component
    public static class ServiceA {
        @Autowired
        ServiceB serviceB;
    }

    @Component
    public static class ServiceB {}

    /** 测试依赖注入 (组件自动装配) */
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

    // ------------------------------------------------------------------------
    // 以下为 @Configuration 与 @Bean 测试用例
    // ------------------------------------------------------------------------

    @Configuration
    public static class AppConfig {
        @Bean
        public ServiceC serviceC() {
            return new ServiceC("bean-created");
        }

        @Bean("specialService")
        public ServiceD serviceD(ServiceC c) {
            return new ServiceD(c.value + "-d");
        }
    }

    public static class ServiceC {
        final String value;
        public ServiceC(String value) { this.value = value; }
    }

    public static class ServiceD {
        final String value;
        public ServiceD(String value) { this.value = value; }
    }

    /** 测试 @Configuration/@Bean 注入与依赖 */
    @Test
    void testBeanAndConfigurationInjection() {
        try (ApplicationContext ctx =
                     new DefaultBootstrapContext(AppConfig.class).build(DefaultApplicationContextFactory.INSTANCE)) {
            ctx.run();

            // 配置类本身被注册为 Bean
            AppConfig configBean = ctx.getBean(AppConfig.class);
            assertNotNull(configBean);

            // Bean 方法自动注册，按方法名获取
            ServiceC c = ctx.getBean(ServiceC.class);
            assertNotNull(c);
            assertEquals("bean-created", c.value);

            // 使用 @Bean("specialService")进行命名，可以按名称、类型获取
            ServiceD d = ctx.getBean("specialService", ServiceD.class);
            assertNotNull(d);
            assertEquals("bean-created-d", d.value);

            // 验证 @Bean 工厂方法参数自动注入
            ServiceD dd = ctx.getBean(ServiceD.class);
            assertNotNull(dd);
            assertEquals("bean-created-d", dd.value);
        }
    }

    /** 测试 @Bean 注解 value 命名规则与缺省规则 */
    @Configuration
    public static class NameConfig {
        @Bean("myFoo")
        public Foo foo() { return new Foo("bar"); }

        @Bean // 未指定 value，则用方法名
        public Foo anotherFoo() { return new Foo("baz"); }
    }

    public static class Foo {
        final String arg;
        public Foo(String arg) { this.arg = arg; }
    }

    @Test
    void testBeanValueRule() {
        try (ApplicationContext ctx =
                     new DefaultBootstrapContext(NameConfig.class).build(DefaultApplicationContextFactory.INSTANCE)) {
            ctx.run();
            Foo foo = ctx.getBean("myFoo", Foo.class);
            assertEquals("bar", foo.arg);

            Foo another = ctx.getBean("anotherFoo", Foo.class);
            assertEquals("baz", another.arg);
        }
    }
}