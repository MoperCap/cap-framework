package org.moper.cap.boot;

import org.junit.jupiter.api.*;
import org.moper.cap.boot.context.DefaultBootstrapContext;
import org.moper.cap.boot.context.DefaultApplicationContextFactory;
import org.moper.cap.context.ApplicationContext;
import org.moper.cap.environment.Environment;
import org.moper.cap.property.officer.PropertyOfficer;
import org.moper.cap.property.publisher.impl.DefaultPropertyPublisher;
import org.moper.cap.property.event.PropertySetOperation;

import static org.junit.jupiter.api.Assertions.*;

class PublisherLifecycleTest {
    public static class Config {}

    @Test
    void testRegisterPublisherAndPropertyPush() {
        try (ApplicationContext ctx =
                     new DefaultBootstrapContext(Config.class).build(DefaultApplicationContextFactory.INSTANCE)) {
            ctx.run();
            Environment env = ctx.getEnvironment();
            PropertyOfficer officer = env.getOfficer();
            DefaultPropertyPublisher publisher = DefaultPropertyPublisher.builder().name("testPublisher").build();
            publisher.contract(officer);
            env.registerPublisher(publisher);

            publisher.publish(new PropertySetOperation("test.key", "value123"));
            // 断言 property 已经可用
            assertEquals("value123",
                env.getViewPool().getPropertyValue("test.key", String.class));
        }
    }
}