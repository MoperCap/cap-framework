package org.moper.cap.example;

import org.moper.cap.boot.context.DefaultBootstrapContext;
import org.moper.cap.boot.context.DefaultApplicationContextFactory;
import org.moper.cap.context.context.ApplicationContext;
import org.moper.cap.example.bean.ServiceDemo;
import org.moper.cap.example.boot.AutowiredBeanDemo;
import org.moper.cap.example.context.ExampleConfig;
import org.moper.cap.example.property.PropertyPublisherDemo;
import org.moper.cap.example.aop.LoggingAspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try(ApplicationContext ctx = new DefaultBootstrapContext(ExampleConfig.class).build(DefaultApplicationContextFactory.INSTANCE)) {
            ctx.run();
            // Bean注入/AOP演示
            ServiceDemo demo = ctx.getBean(ServiceDemo.class);
            System.out.println(demo.greet("World"));
            
            AutowiredBeanDemo autoBean = ctx.getBean(AutowiredBeanDemo.class);
            autoBean.showGreeting();

            // 属性系统演示
            PropertyPublisherDemo.publishDemo(ctx.getPropertyOfficer());

            // AOP演示（aspect会自动切入）
            LoggingAspect aspect = ctx.getBean(LoggingAspect.class); // 验证aspect是否生效
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}