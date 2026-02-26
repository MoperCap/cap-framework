package org.moper.cap.property.officer.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.moper.cap.property.event.PropertyManifest;
import org.moper.cap.property.event.PropertyOperation;
import org.moper.cap.property.event.PropertySetOperation;
import org.moper.cap.property.event.PropertyRemoveOperation;
import org.moper.cap.property.exception.PropertyValidationException;
import org.moper.cap.property.publisher.PropertyPublisher;
import org.moper.cap.property.subscriber.PropertySubscriber;
import org.moper.cap.property.subscriber.impl.DefaultAbstractPropertySubscriber;
import org.moper.cap.property.subscriber.impl.DefaultPropertySubscription;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class DefaultPropertyOfficerTest {

    private DefaultPropertyOfficer officer;

    @BeforeEach
    void setUp() {
        officer = new DefaultPropertyOfficer();
    }

    @AfterEach
    void tearDown() throws Exception {
        officer.close();
    }

    @Test
    void testOnlyPublisherCanModifyProperty() {
        // 注册 publisher
        String pubName = "pub1";
        PropertyPublisher publisher = officer.getPublisher(pubName);
        String key = "test.key";

        // 订阅属性
        AtomicInteger subValue = new AtomicInteger(0);
        PropertySubscriber<Integer> subscriber = new DefaultAbstractPropertySubscriber<>(key, Integer.class) {
            @Override public void onSet(Integer value) {
                log.debug("onSet: {}", value);
                subValue.set(value);
            }
            @Override public void onRemoved() {
                subValue.set(-1);
            }
        };
        officer.createSubscription(() -> new DefaultPropertySubscription(List.of(subscriber)));

        // 正确通过 publisher 封装属性变更并发布
        publisher.publish(new PropertySetOperation(key, 100));
        assertTrue(officer.containsProperty(key));
        assertEquals(100, officer.getRawPropertyValue(key));
        assertEquals(100, subValue.get());

        publisher.publish(new PropertyRemoveOperation(key));
        assertFalse(officer.containsProperty(key));
        assertEquals(-1, subValue.get());
    }

    @Test
    void testOfficerRejectsNonPublisherManifest() {
        // 没有注册对应publisher
        String fakeOperator = "nonExistentPub";
        String key = "nope.key";
        PropertyManifest manifest = PropertyManifest.of(fakeOperator, List.of(new PropertySetOperation(key, 999)));
        // 直接调用receive必须抛异常
        assertThrows(PropertyValidationException.class, () -> officer.receive(manifest));
        // 异步也必须异常
        assertThrows(PropertyValidationException.class, () -> officer.receiveAsync(manifest));
    }

    @Test
    void testRejectUnregisteredPublisherPublish() {
        // 假的publisher（未登记在officer中），直接publish必须异常
        PropertyPublisher fakePub = new PropertyPublisher() {
            @Override public String name() { return "fake"; }

            @Override
            public void publish(PropertyOperation... operations) {
                PropertyManifest manifest = PropertyManifest.of(name(), List.of(operations));
                // 模拟直接推官员，必须被拒绝
                assertThrows(PropertyValidationException.class, () -> officer.receive(manifest));
            }

            @Override
            public void publishAsync(PropertyOperation... operations) {
                PropertyManifest manifest = PropertyManifest.of(name(), List.of(operations));
                assertThrows(PropertyValidationException.class, () -> officer.receive(manifest));
            }

            @Override
            public boolean isClosed() {
                return false;
            }

            @Override public void close() {}
        };
        fakePub.publish(new PropertySetOperation("x", 1));
        fakePub.publish(new PropertyRemoveOperation("x"));
    }

}