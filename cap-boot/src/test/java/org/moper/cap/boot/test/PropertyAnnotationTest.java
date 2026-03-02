package org.moper.cap.boot.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.boot.application.impl.DefaultCapApplication;
import org.moper.cap.core.annotation.ComponentScan;
import org.moper.cap.core.annotation.ResourceScan;
import org.moper.cap.core.context.RuntimeContext;
import org.moper.cap.property.annotation.Subscriber;
import org.moper.cap.property.annotation.Subscription;
import org.moper.cap.property.annotation.Value;
import org.moper.cap.property.event.PropertyRemoveOperation;
import org.moper.cap.property.event.PropertySetOperation;
import org.moper.cap.property.officer.PropertyOfficer;
import org.moper.cap.property.publisher.PropertyPublisher;

@Slf4j
public class PropertyAnnotationTest {

    /* ──────────────────── @Value 注入测试 ──────────────────── */

    @ComponentScan("org.moper.cap.boot.test")
    @ResourceScan("true")
    public static class ValueConfig {
    }

    @Capper
    public static class ValueBean {
        @Value("${student.name}")
        private String name;

        @Value("${student.age}")
        private Integer age;

        @Value("${missing.key:defaultValue}")
        private String withDefault;

        @Value("${missing.int:42}")
        private Integer intWithDefault;
    }

    @Test
    void valueInjectionTest() throws Exception {
        try (RuntimeContext context = new DefaultCapApplication(ValueConfig.class).run()) {
            ValueBean bean = context.getBean("valueBean", ValueBean.class);
            Assertions.assertEquals("wang", bean.name);
            Assertions.assertEquals(21, bean.age);
            Assertions.assertEquals("defaultValue", bean.withDefault);
            Assertions.assertEquals(42, bean.intWithDefault);
        }
    }

    /* ──────────────────── @Subscription / @Subscriber 测试 ──────────────────── */

    @Capper
    @Subscription("testSubscription")
    public static class SubscriptionBean {

        @Subscriber(propertyKey = "dynamic.value", onSet = "onDynamicValueSet", onRemoved = "onDynamicValueRemoved")
        private String dynamicValue;

        private String lastSetValue;
        private boolean removedCalled;

        void onDynamicValueSet(String newVal) {
            this.lastSetValue = newVal;
            log.info("onDynamicValueSet: {}", newVal);
        }

        void onDynamicValueRemoved() {
            this.removedCalled = true;
            log.info("onDynamicValueRemoved");
        }
    }

    @Test
    void subscriptionTest() throws Exception {
        try (RuntimeContext context = new DefaultCapApplication(ValueConfig.class).run()) {
            PropertyOfficer officer = context.getPropertyOfficer();

            // Register a publisher and set/remove a property
            PropertyPublisher publisher = officer.getPublisher("test-publisher");

            SubscriptionBean bean = context.getBean("subscriptionBean", SubscriptionBean.class);

            publisher.publish(new PropertySetOperation("dynamic.value", "hello"));
            Assertions.assertEquals("hello", bean.dynamicValue);
            Assertions.assertEquals("hello", bean.lastSetValue);

            publisher.publish(new PropertySetOperation("dynamic.value", "world"));
            Assertions.assertEquals("world", bean.dynamicValue);
            Assertions.assertEquals("world", bean.lastSetValue);

            publisher.publish(new PropertyRemoveOperation("dynamic.value"));
            Assertions.assertTrue(bean.removedCalled);
        }
    }

    /* ──────────────────── @Subscriber 无回调方法测试 ──────────────────── */

    @Capper
    @Subscription
    public static class SimpleSubscriptionBean {

        @Subscriber(propertyKey = "simple.value")
        private String simpleValue;
    }

    @Test
    void simpleSubscriptionTest() throws Exception {
        try (RuntimeContext context = new DefaultCapApplication(ValueConfig.class).run()) {
            PropertyOfficer officer = context.getPropertyOfficer();
            PropertyPublisher publisher = officer.getPublisher("simple-publisher");

            SimpleSubscriptionBean bean = context.getBean("simpleSubscriptionBean", SimpleSubscriptionBean.class);

            publisher.publish(new PropertySetOperation("simple.value", "test123"));
            Assertions.assertEquals("test123", bean.simpleValue);
        }
    }
}
