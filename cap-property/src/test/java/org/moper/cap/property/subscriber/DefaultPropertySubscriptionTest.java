package org.moper.cap.property.subscriber;

import org.junit.jupiter.api.*;
import org.moper.cap.property.event.PropertyOperation;
import org.moper.cap.property.event.PropertyRemoveOperation;
import org.moper.cap.property.event.PropertySetOperation;
import org.moper.cap.property.officer.PropertyOfficer;
import org.moper.cap.property.subscriber.selector.AnyPropertySelector;
import org.moper.cap.property.subscriber.selector.ExactPropertySelector;
import org.moper.cap.property.subscriber.subcription.DefaultPropertySubscription;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DefaultPropertySubscription 测试")
class DefaultPropertySubscriptionTest {

    // ===================== 构造与基本属性 =====================

    @Test
    @DisplayName("使用显式 Selector 构造成功")
    void testConstructWithExplicitSelector() {
        List<PropertySubscriber> subs = List.of(new RecordingSubscriber("key"));
        DefaultPropertySubscription sub = new DefaultPropertySubscription(
                "test-sub", new AnyPropertySelector(), subs);

        assertEquals("test-sub", sub.name());
        assertNotNull(sub.selector());
        assertFalse(sub.isClosed());
    }

    @Test
    @DisplayName("使用订阅者列表自动生成 ExactPropertySelector")
    void testConstructWithAutoSelector() {
        List<PropertySubscriber> subs = List.of(
                new RecordingSubscriber("key1"),
                new RecordingSubscriber("key2"));
        DefaultPropertySubscription sub = new DefaultPropertySubscription("auto-sub", subs);

        assertEquals("auto-sub", sub.name());
        assertTrue(sub.selector().matches("key1"));
        assertTrue(sub.selector().matches("key2"));
        assertFalse(sub.selector().matches("other.key"));
    }

    @Test
    @DisplayName("自动生成的 Selector 是 ExactPropertySelector 实例")
    void testAutoSelectorType() {
        List<PropertySubscriber> subs = List.of(new RecordingSubscriber("k"));
        DefaultPropertySubscription sub = new DefaultPropertySubscription("s", subs);

        assertInstanceOf(ExactPropertySelector.class, sub.selector());
    }

    // ===================== dispatch - set 操作 =====================

    @Test
    @DisplayName("dispatch set 操作：订阅者收到 onSet 回调")
    void testDispatchSetOperation() throws Exception {
        RecordingSubscriber subscriber = new RecordingSubscriber("db.host");
        DefaultPropertySubscription sub = new DefaultPropertySubscription(
                "sub", new AnyPropertySelector(), List.of(subscriber));

        sub.dispatch(new PropertySetOperation("db.host", "localhost"));

        assertEquals(1, subscriber.setValues.size());
        assertEquals("localhost", subscriber.setValues.get(0));
    }

    @Test
    @DisplayName("dispatch set 操作：不匹配的订阅者不收到通知")
    void testDispatchSetOperationNotMatchingSubscriber() {
        RecordingSubscriber subscriber = new RecordingSubscriber("other.key");
        DefaultPropertySubscription sub = new DefaultPropertySubscription(
                "sub", new AnyPropertySelector(), List.of(subscriber));

        sub.dispatch(new PropertySetOperation("db.host", "localhost"));

        assertTrue(subscriber.setValues.isEmpty());
    }

    @Test
    @DisplayName("dispatch set 操作：null 值正常传递")
    void testDispatchSetOperationWithNullValue() {
        RecordingSubscriber subscriber = new RecordingSubscriber("key");
        DefaultPropertySubscription sub = new DefaultPropertySubscription(
                "sub", new AnyPropertySelector(), List.of(subscriber));

        sub.dispatch(new PropertySetOperation("key", null));

        assertEquals(1, subscriber.setValues.size());
        assertNull(subscriber.setValues.get(0));
    }

    @Test
    @DisplayName("dispatch 多个 set 操作：各自通知对应订阅者")
    void testDispatchMultipleSetOperations() {
        RecordingSubscriber subHost = new RecordingSubscriber("db.host");
        RecordingSubscriber subPort = new RecordingSubscriber("db.port");
        DefaultPropertySubscription sub = new DefaultPropertySubscription(
                "sub", new AnyPropertySelector(), List.of(subHost, subPort));

        sub.dispatch(
                new PropertySetOperation("db.host", "localhost"),
                new PropertySetOperation("db.port", 3306)
        );

        assertEquals(1, subHost.setValues.size());
        assertEquals("localhost", subHost.setValues.get(0));
        assertEquals(1, subPort.setValues.size());
        assertEquals(3306, subPort.setValues.get(0));
    }

    // ===================== dispatch - remove 操作 =====================

    @Test
    @DisplayName("dispatch remove 操作：订阅者收到 onRemoved 回调")
    void testDispatchRemoveOperation() {
        RecordingSubscriber subscriber = new RecordingSubscriber("db.host");
        DefaultPropertySubscription sub = new DefaultPropertySubscription(
                "sub", new AnyPropertySelector(), List.of(subscriber));

        sub.dispatch(new PropertyRemoveOperation("db.host"));

        assertEquals(1, subscriber.removedCount.get());
    }

    @Test
    @DisplayName("dispatch remove 操作：不匹配的订阅者不收到通知")
    void testDispatchRemoveOperationNotMatchingSubscriber() {
        RecordingSubscriber subscriber = new RecordingSubscriber("other.key");
        DefaultPropertySubscription sub = new DefaultPropertySubscription(
                "sub", new AnyPropertySelector(), List.of(subscriber));

        sub.dispatch(new PropertyRemoveOperation("db.host"));

        assertEquals(0, subscriber.removedCount.get());
    }

    // ===================== dispatch - 混合操作 =====================

    @Test
    @DisplayName("dispatch 混合 set 和 remove 操作")
    void testDispatchMixedOperations() {
        RecordingSubscriber subscriber = new RecordingSubscriber("key");
        DefaultPropertySubscription sub = new DefaultPropertySubscription(
                "sub", new AnyPropertySelector(), List.of(subscriber));

        sub.dispatch(
                new PropertySetOperation("key", "value"),
                new PropertyRemoveOperation("key")
        );

        assertEquals(1, subscriber.setValues.size());
        assertEquals(1, subscriber.removedCount.get());
    }

    // ===================== 异常容错 =====================

    @Test
    @DisplayName("订阅者抛出异常不影响其他订阅者")
    void testSubscriberExceptionDoesNotAffectOthers() {
        ThrowingSubscriber throwingSub = new ThrowingSubscriber("key");
        RecordingSubscriber normalSub = new RecordingSubscriber("key");
        DefaultPropertySubscription sub = new DefaultPropertySubscription(
                "sub", new AnyPropertySelector(), List.of(throwingSub, normalSub));

        assertDoesNotThrow(() ->
                sub.dispatch(new PropertySetOperation("key", "value")));

        assertEquals(1, normalSub.setValues.size());
    }

    @Test
    @DisplayName("订阅者 onRemoved 抛出异常不影响其他订阅者")
    void testSubscriberRemoveExceptionDoesNotAffectOthers() {
        ThrowingSubscriber throwingSub = new ThrowingSubscriber("key");
        RecordingSubscriber normalSub = new RecordingSubscriber("key");
        DefaultPropertySubscription sub = new DefaultPropertySubscription(
                "sub", new AnyPropertySelector(), List.of(throwingSub, normalSub));

        assertDoesNotThrow(() ->
                sub.dispatch(new PropertyRemoveOperation("key")));

        assertEquals(1, normalSub.removedCount.get());
    }

    // ===================== 关闭行为 =====================

    @Test
    @DisplayName("close 后 isClosed 返回 true")
    void testCloseSetsClosed() {
        DefaultPropertySubscription sub = new DefaultPropertySubscription(
                "sub", new AnyPropertySelector(), List.of(new RecordingSubscriber("k")));
        sub.close();
        assertTrue(sub.isClosed());
    }

    @Test
    @DisplayName("close 后 dispatch 忽略操作")
    void testDispatchAfterClosed() {
        RecordingSubscriber subscriber = new RecordingSubscriber("key");
        DefaultPropertySubscription sub = new DefaultPropertySubscription(
                "sub", new AnyPropertySelector(), List.of(subscriber));
        sub.close();

        sub.dispatch(new PropertySetOperation("key", "value"));

        assertTrue(subscriber.setValues.isEmpty());
    }

    @Test
    @DisplayName("close 幂等")
    void testCloseIdempotent() {
        DefaultPropertySubscription sub = new DefaultPropertySubscription(
                "sub", new AnyPropertySelector(), List.of(new RecordingSubscriber("k")));
        assertDoesNotThrow(() -> {
            sub.close();
            sub.close();
        });
    }

    // ===================== offOfficer =====================

    @Test
    @DisplayName("offOfficer 触发所有订阅者 onRemoved")
    void testOffOfficerNotifiesAllSubscribers() {
        RecordingSubscriber sub1 = new RecordingSubscriber("k1");
        RecordingSubscriber sub2 = new RecordingSubscriber("k2");
        DefaultPropertySubscription sub = new DefaultPropertySubscription(
                "sub", new AnyPropertySelector(), List.of(sub1, sub2));

        sub.offOfficer(null);

        assertEquals(1, sub1.removedCount.get());
        assertEquals(1, sub2.removedCount.get());
    }

    @Test
    @DisplayName("offOfficer 中订阅者抛异常不影响其他订阅者")
    void testOffOfficerExceptionDoesNotAffectOthers() {
        ThrowingSubscriber throwingSub = new ThrowingSubscriber("k1");
        RecordingSubscriber normalSub = new RecordingSubscriber("k2");
        DefaultPropertySubscription sub = new DefaultPropertySubscription(
                "sub", new AnyPropertySelector(), List.of(throwingSub, normalSub));

        assertDoesNotThrow(() -> sub.offOfficer(null));

        assertEquals(1, normalSub.removedCount.get());
    }

    // ===================== 测试辅助类 =====================

    /**
     * 记录 onSet / onRemoved 调用的订阅者
     */
    static class RecordingSubscriber extends AbstractSubscriber {
        final List<Object> setValues = new ArrayList<>();
        final AtomicInteger removedCount = new AtomicInteger(0);

        RecordingSubscriber(String key) { super(key); }

        @Override
        public void onSet(Object value) { setValues.add(value); }

        @Override
        public void onRemoved() { removedCount.incrementAndGet(); }
    }

    /**
     * 每次都抛出异常的订阅者
     */
    static class ThrowingSubscriber extends AbstractSubscriber {
        ThrowingSubscriber(String key) { super(key); }

        @Override
        public void onSet(Object value) throws Exception {
            throw new RuntimeException("intentional test exception in onSet");
        }

        @Override
        public void onRemoved() throws Exception {
            throw new RuntimeException("intentional test exception in onRemoved");
        }
    }
}
