package org.moper.cap.boot;

import org.junit.jupiter.api.Test;
import org.moper.cap.annotation.Subscription;
import org.moper.cap.annotation.Subscriber;
import org.moper.cap.boot.context.DefaultBootstrapContext;
import org.moper.cap.boot.context.DefaultApplicationContextFactory;
import org.moper.cap.context.ApplicationContext;
import org.moper.cap.environment.Environment;
import org.moper.cap.property.officer.PropertyOfficer;
import org.moper.cap.property.publisher.impl.DefaultPropertyPublisher;
import org.moper.cap.property.event.PropertySetOperation;

import static org.junit.jupiter.api.Assertions.*;

public class SubscriptionIntegrationTest {

    @Subscription("testSub")
    public static class Bean {
        @Subscriber(propertyKey = "alpha", onSet = "setCallback", onRemoved = "removeCallback")
        public String alpha;

        public boolean setFired = false;
        public boolean removeFired = false;

        public void setCallback(Object val) { setFired = true; }
        public void removeCallback() { removeFired = true; }
    }

    @Test
    void testDynamicUpdate() {
        try (ApplicationContext ctx =
                new DefaultBootstrapContext(Bean.class).build(DefaultApplicationContextFactory.INSTANCE)) {
            ctx.run();
            Bean bean = ctx.getBean(Bean.class);
            Environment env = ctx.getEnvironment();
            PropertyOfficer officer = env.getOfficer();

            DefaultPropertyPublisher publisher = DefaultPropertyPublisher.builder().name("p1").build();
            publisher.contract(officer);
            env.registerPublisher(publisher);

            publisher.publish(new PropertySetOperation("alpha", "HELLO"));

            assertEquals("HELLO", bean.alpha);
            assertTrue(bean.setFired);

            // publisher.publish(new PropertyRemoveOperation("alpha"));
            // assertNull(bean.alpha);
            // assertTrue(bean.removeFired);
        }
    }
}