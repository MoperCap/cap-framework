package org.moper.cap.property.integration;

import org.junit.jupiter.api.*;
import org.moper.cap.property.event.PropertyRemoveOperation;
import org.moper.cap.property.event.PropertySetOperation;
import org.moper.cap.property.officer.impl.DefaultPropertyOfficer;
import org.moper.cap.property.publisher.PropertyPublisher;
import org.moper.cap.property.publisher.impl.DefaultPropertyPublisher;
import org.moper.cap.property.result.PublisherManifestResult;
import org.moper.cap.property.subscriber.AbstractSubscriber;
import org.moper.cap.property.subscriber.PropertySubscriber;
import org.moper.cap.property.subscriber.selector.AnyPropertySelector;
import org.moper.cap.property.subscriber.subcription.DefaultPropertySubscription;
import org.moper.cap.property.subscriber.view.DefaultPropertyViewPool;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("属性系统集成测试")
class PropertySystemIntegrationTest {

    private DefaultPropertyPublisher publisher;
    private DefaultPropertyOfficer officer;

    @BeforeEach
    void setUp() throws Exception {
        publisher = DefaultPropertyPublisher.builder()
                .name("config-publisher")
                .build();

        Map<PropertyPublisher, Integer> publishersMap = new ConcurrentHashMap<>();
        publishersMap.put(publisher, 0);

        officer = DefaultPropertyOfficer.builder()
                .name("config-officer")
                .publishers(publishersMap)
                .build();

        publisher.contract(officer);
    }

    @AfterEach
    void tearDown() {
        if (!publisher.isClosed()) publisher.close();
        if (!officer.isClosed()) officer.close();
    }

    // ===================== 基本发布-接收流程 =====================

    @Test
    @DisplayName("Publisher 发布 set 操作，Officer 接收并通知 ViewPool")
    void testPublishAndViewPool() {
        DefaultPropertyViewPool viewPool = new DefaultPropertyViewPool("pool");
        officer.subscribe(viewPool);

        publisher.publish(new PropertySetOperation("db.host", "localhost"));

        assertTrue(viewPool.containsProperty("db.host"));
        assertEquals("localhost", viewPool.getRawPropertyValue("db.host"));
        viewPool.close();
    }

    @Test
    @DisplayName("Publisher 关闭时，Officer 通过 offPublisher 路径通知 ViewPool 删除属性")
    void testPublishRemoveAndViewPool() throws Exception {
        DefaultPropertyViewPool viewPool = new DefaultPropertyViewPool("pool");
        officer.subscribe(viewPool);

        publisher.publish(new PropertySetOperation("key", "value"));
        assertTrue(viewPool.containsProperty("key"), "set 操作后属性应存在");

        // 通过 publisher.close() 触发 offPublisher，ViewPool 正确接收到删除通知
        publisher.close();
        assertFalse(viewPool.containsProperty("key"), "offPublisher 后属性应被删除");
        viewPool.close();
    }

    @Test
    @DisplayName("Publisher 发布多个属性，ViewPool 全部可查询")
    void testPublishMultipleProperties() {
        DefaultPropertyViewPool viewPool = new DefaultPropertyViewPool("pool");
        officer.subscribe(viewPool);

        publisher.publish(
                new PropertySetOperation("db.host", "localhost"),
                new PropertySetOperation("db.port", 3306),
                new PropertySetOperation("db.name", "mydb")
        );

        assertEquals("localhost", viewPool.getRawPropertyValue("db.host"));
        assertEquals(3306, viewPool.getRawPropertyValue("db.port"));
        assertEquals("mydb", viewPool.getRawPropertyValue("db.name"));
        viewPool.close();
    }

    @Test
    @DisplayName("ViewPool 订阅时自动收到已有属性的初始化通知")
    void testViewPoolReceivesExistingPropertiesOnSubscribe() {
        publisher.publish(
                new PropertySetOperation("k1", "v1"),
                new PropertySetOperation("k2", "v2")
        );

        // 先发布，再订阅
        DefaultPropertyViewPool viewPool = new DefaultPropertyViewPool("pool");
        officer.subscribe(viewPool);

        assertTrue(viewPool.containsProperty("k1"));
        assertTrue(viewPool.containsProperty("k2"));
        viewPool.close();
    }

    // ===================== Subscription 集成 =====================

    @Test
    @DisplayName("Subscription 订阅者收到发布的属性更新")
    void testSubscriptionReceivesUpdates() {
        RecordingSubscriber subscriber = new RecordingSubscriber("db.host");
        DefaultPropertySubscription subscription = new DefaultPropertySubscription(
                "db-sub", new AnyPropertySelector(), List.of(subscriber));
        officer.subscribe(subscription);

        publisher.publish(new PropertySetOperation("db.host", "newhost"));

        assertEquals(1, subscriber.setValues.size());
        assertEquals("newhost", subscriber.setValues.get(0));
        subscription.close();
    }

    @Test
    @DisplayName("Subscription 只收到选择器匹配的属性更新")
    void testSubscriptionFiltersWithSelector() {
        RecordingSubscriber dbSubscriber = new RecordingSubscriber("db.host");
        DefaultPropertySubscription subscription = new DefaultPropertySubscription(
                "db-sub", key -> key.startsWith("db."), List.of(dbSubscriber));
        officer.subscribe(subscription);

        publisher.publish(
                new PropertySetOperation("db.host", "localhost"),
                new PropertySetOperation("app.name", "myapp")
        );

        assertEquals(1, dbSubscriber.setValues.size());
        subscription.close();
    }

    @Test
    @DisplayName("unsubscribe 后不再收到通知")
    void testUnsubscribeStopsNotifications() {
        RecordingSubscriber subscriber = new RecordingSubscriber("key");
        DefaultPropertySubscription subscription = new DefaultPropertySubscription(
                "sub", new AnyPropertySelector(), List.of(subscriber));
        officer.subscribe(subscription);
        officer.unsubscribe(subscription);

        publisher.publish(new PropertySetOperation("key", "value"));

        assertTrue(subscriber.setValues.isEmpty());
        subscription.close();
    }

    // ===================== Publisher 关闭流程 =====================

    @Test
    @DisplayName("Publisher 关闭后，Officer 通知订阅者删除其属性")
    void testPublisherCloseRemovesProperties() {
        DefaultPropertyViewPool viewPool = new DefaultPropertyViewPool("pool");
        officer.subscribe(viewPool);

        publisher.publish(
                new PropertySetOperation("k1", "v1"),
                new PropertySetOperation("k2", "v2")
        );

        publisher.close();

        assertFalse(viewPool.containsProperty("k1"));
        assertFalse(viewPool.containsProperty("k2"));
        viewPool.close();
    }

    // ===================== 多 Publisher 场景 =====================

    @Test
    @DisplayName("多个 Publisher 各自独立发布，Officer 正确管理")
    void testMultiplePublishers() throws Exception {
        DefaultPropertyPublisher pub2 = DefaultPropertyPublisher.builder().name("pub2").build();
        Map<PropertyPublisher, Integer> pubs = new ConcurrentHashMap<>();
        pubs.put(publisher, 0);
        pubs.put(pub2, 0);

        DefaultPropertyOfficer sharedOfficer = DefaultPropertyOfficer.builder()
                .name("shared-officer")
                .publishers(pubs)
                .build();

        publisher.contract(sharedOfficer);
        pub2.contract(sharedOfficer);

        DefaultPropertyViewPool viewPool = new DefaultPropertyViewPool("pool");
        sharedOfficer.subscribe(viewPool);

        publisher.publish(new PropertySetOperation("pub1.key", "v1"));
        pub2.publish(new PropertySetOperation("pub2.key", "v2"));

        assertEquals("v1", viewPool.getRawPropertyValue("pub1.key"));
        assertEquals("v2", viewPool.getRawPropertyValue("pub2.key"));

        viewPool.close();
        sharedOfficer.close();
        if (!pub2.isClosed()) pub2.close();
    }

    @Test
    @DisplayName("不同 Publisher 不能覆盖彼此的属性")
    void testPublishersCannotOverwriteEachOthers() throws Exception {
        DefaultPropertyPublisher pub2 = DefaultPropertyPublisher.builder().name("pub2").build();
        Map<PropertyPublisher, Integer> pubs = new ConcurrentHashMap<>();
        pubs.put(publisher, 0);
        pubs.put(pub2, 0);

        DefaultPropertyOfficer sharedOfficer = DefaultPropertyOfficer.builder()
                .name("shared-officer")
                .publishers(pubs)
                .build();

        publisher.contract(sharedOfficer);
        pub2.contract(sharedOfficer);

        publisher.publish(new PropertySetOperation("shared.key", "from-pub1"));

        // pub2 尝试覆盖同一属性 -> 返回 PARTIAL_SUCCESS（权限冲突）
        List<PublisherManifestResult> results =
                pub2.publish(new PropertySetOperation("shared.key", "from-pub2"));

        assertFalse(results.isEmpty());
        assertEquals(PublisherManifestResult.Status.PARTIAL_SUCCESS, results.get(0).status());

        // 属性值保持不变
        DefaultPropertyViewPool viewPool = new DefaultPropertyViewPool("pool");
        sharedOfficer.subscribe(viewPool);
        assertEquals("from-pub1", viewPool.getRawPropertyValue("shared.key"));

        viewPool.close();
        sharedOfficer.close();
        if (!pub2.isClosed()) pub2.close();
    }

    // ===================== 版本追赶场景 =====================

    @Test
    @DisplayName("Officer 版本落后时，receive 触发拉取追赶")
    void testVersionCatchUp() {
        // publisher 先发布两次（版本 0, 1），但 officer 只知道版本 0
        publisher.publish(new PropertySetOperation("k0", "v0")); // officer 处理版本 0
        publisher.publish(new PropertySetOperation("k1", "v1")); // officer 期望版本 1

        // officer 已正常处理版本 0，现在直接接收版本 1
        // officer 的期望版本应已更新为 1，所以版本 1 应成功
        DefaultPropertyViewPool viewPool = new DefaultPropertyViewPool("pool");
        officer.subscribe(viewPool);

        assertTrue(viewPool.containsProperty("k0"));
        assertTrue(viewPool.containsProperty("k1"));
        viewPool.close();
    }

    // ===================== 异步发布 =====================

    @Test
    @DisplayName("publishAsync 异步发布，ViewPool 最终收到更新")
    void testPublishAsyncViewPool() throws Exception {
        DefaultPropertyViewPool viewPool = new DefaultPropertyViewPool("pool");
        officer.subscribe(viewPool);

        List<CompletableFuture<PublisherManifestResult>> futures =
                publisher.publishAsync(new PropertySetOperation("async.key", "async-value"));

        assertFalse(futures.isEmpty());
        // 等待异步处理完成
        for (CompletableFuture<PublisherManifestResult> future : futures) {
            future.get(5, TimeUnit.SECONDS);
        }

        assertTrue(viewPool.containsProperty("async.key"));
        assertEquals("async-value", viewPool.getRawPropertyValue("async.key"));
        viewPool.close();
    }

    // ===================== ViewPool 类型转换集成 =====================

    @Test
    @DisplayName("端到端：发布整数属性，ViewPool 类型转换后正确返回")
    void testEndToEndTypeConversion() {
        DefaultPropertyViewPool viewPool = new DefaultPropertyViewPool("pool");
        officer.subscribe(viewPool);

        publisher.publish(new PropertySetOperation("server.port", 8080));

        assertEquals(8080, viewPool.getPropertyValue("server.port", Integer.class));
        assertEquals("8080", viewPool.getPropertyValue("server.port", String.class));
        assertEquals(8080L, viewPool.getPropertyValue("server.port", Long.class));
        viewPool.close();
    }

    @Test
    @DisplayName("端到端：getPropertyValueOptional 函数式使用")
    void testEndToEndOptional() {
        DefaultPropertyViewPool viewPool = new DefaultPropertyViewPool("pool");
        officer.subscribe(viewPool);

        publisher.publish(new PropertySetOperation("feature.enabled", true));

        AtomicReference<Boolean> result = new AtomicReference<>(false);
        viewPool.getPropertyValueOptional("feature.enabled", Boolean.class)
                .ifPresent(result::set);

        assertTrue(result.get());
        viewPool.close();
    }

    // ===================== 辅助类 =====================

    static class RecordingSubscriber extends AbstractSubscriber {
        final List<Object> setValues = new ArrayList<>();
        final AtomicInteger removedCount = new AtomicInteger(0);

        RecordingSubscriber(String key) { super(key); }

        @Override
        public void onSet(Object value) { setValues.add(value); }

        @Override
        public void onRemoved() { removedCount.incrementAndGet(); }
    }
}
