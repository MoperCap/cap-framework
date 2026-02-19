package org.moper.cap.property.officer;

import org.junit.jupiter.api.*;
import org.moper.cap.property.event.PropertyOperation;
import org.moper.cap.property.event.PropertyRemoveOperation;
import org.moper.cap.property.event.PropertySetOperation;
import org.moper.cap.property.event.PublisherManifest;
import org.moper.cap.property.officer.impl.DefaultPropertyOfficer;
import org.moper.cap.property.publisher.PropertyPublisher;
import org.moper.cap.property.publisher.impl.DefaultPropertyPublisher;
import org.moper.cap.property.result.PublisherManifestResult;
import org.moper.cap.property.subscriber.PropertySelector;
import org.moper.cap.property.subscriber.PropertySubscription;
import org.moper.cap.property.subscriber.selector.AnyPropertySelector;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DefaultPropertyOfficer 测试")
class DefaultPropertyOfficerTest {

    private DefaultPropertyPublisher publisher;
    private DefaultPropertyOfficer officer;

    @BeforeEach
    void setUp() {
        publisher = DefaultPropertyPublisher.builder()
                .name("pub")
                .build();

        // 将 publisher 预注册到 officer，期望版本从 0 开始
        Map<PropertyPublisher, Integer> publishers = new ConcurrentHashMap<>();
        publishers.put(publisher, 0);

        officer = DefaultPropertyOfficer.builder()
                .name("officer")
                .publishers(publishers)
                .build();
    }

    @AfterEach
    void tearDown() {
        if (!publisher.isClosed()) publisher.close();
        if (!officer.isClosed()) officer.close();
    }

    // ===================== 基本属性 =====================

    @Test
    @DisplayName("正确返回 name")
    void testName() {
        assertEquals("officer", officer.name());
    }

    @Test
    @DisplayName("初始版本为 0")
    void testInitialVersion() {
        assertEquals(0, officer.currentVersion());
    }

    @Test
    @DisplayName("初始未关闭")
    void testInitialNotClosed() {
        assertFalse(officer.isClosed());
    }

    // ===================== receive - 正常处理 =====================

    @Test
    @DisplayName("receive 成功处理 set 操作，版本递增")
    void testReceiveSetOperation() {
        PublisherManifest manifest = buildManifest(0, new PropertySetOperation("db.host", "localhost"));

        PublisherManifestResult result = officer.receive(manifest);

        assertEquals(PublisherManifestResult.Status.TOTAL_SUCCESS, result.status());
        assertEquals(1, officer.currentVersion());
    }

    @Test
    @DisplayName("receive 成功处理 remove 操作")
    void testReceiveRemoveOperation() {
        // 先设置属性
        officer.receive(buildManifest(0, new PropertySetOperation("key", "value")));
        // 再删除
        PublisherManifestResult result = officer.receive(buildManifest(1, new PropertyRemoveOperation("key")));

        assertEquals(PublisherManifestResult.Status.TOTAL_SUCCESS, result.status());
    }

    @Test
    @DisplayName("receive 处理多个操作，部分成功返回 PARTIAL_SUCCESS")
    void testReceivePartialSuccess() {
        // 先让另一个 publisher 拥有 key1
        DefaultPropertyPublisher pub2 = DefaultPropertyPublisher.builder().name("pub2").build();
        Map<PropertyPublisher, Integer> pubs2 = new ConcurrentHashMap<>();
        pubs2.put(pub2, 0);
        DefaultPropertyOfficer officer2 = DefaultPropertyOfficer.builder()
                .name("officer2").publishers(pubs2).build();

        // pub2 设置 conflicting.key
        officer2.receive(buildManifest(pub2, 0, new PropertySetOperation("conflicting.key", "v")));
        // 现在 pub2's officer2 已有 conflicting.key；但这是另一个 officer

        // 在 officer 中，由 pub 设置 key1
        officer.receive(buildManifest(0, new PropertySetOperation("key1", "v1")));

        // 现在尝试用同一个 publisher 的不同 key 来混合：
        // 设置 key1 成功（更新自己的），同时试图删除 nonexistent-key（失败）
        PublisherManifestResult result = officer.receive(buildManifest(1,
                new PropertySetOperation("key1", "v1-updated"),
                new PropertyRemoveOperation("nonexistent-key")));

        assertEquals(PublisherManifestResult.Status.PARTIAL_SUCCESS, result.status());
        officer2.close();
        if (!pub2.isClosed()) pub2.close();
    }

    @Test
    @DisplayName("receive 未知发布者返回 ERROR")
    void testReceiveUnknownPublisher() {
        DefaultPropertyPublisher unknownPub = DefaultPropertyPublisher.builder().name("unknown").build();
        PublisherManifest manifest = buildManifest(unknownPub, 0, new PropertySetOperation("k", "v"));

        PublisherManifestResult result = officer.receive(manifest);

        assertEquals(PublisherManifestResult.Status.ERROR, result.status());
        unknownPub.close();
    }

    @Test
    @DisplayName("receive 版本过旧返回 SKIP")
    void testReceiveOldVersionSkipped() {
        // 处理版本 0
        officer.receive(buildManifest(0, new PropertySetOperation("k", "v")));
        // 再次处理版本 0（旧版本）
        PublisherManifestResult result = officer.receive(buildManifest(0, new PropertySetOperation("k2", "v2")));

        assertEquals(PublisherManifestResult.Status.SKIP, result.status());
    }

    @Test
    @DisplayName("receive 关闭后返回 ERROR")
    void testReceiveAfterClosed() {
        officer.close();
        PublisherManifest manifest = buildManifest(0, new PropertySetOperation("k", "v"));

        PublisherManifestResult result = officer.receive(manifest);

        assertEquals(PublisherManifestResult.Status.ERROR, result.status());
    }

    // ===================== 权限冲突 =====================

    @Test
    @DisplayName("不同发布者试图设置同名属性时返回 PARTIAL_SUCCESS（权限冲突）")
    void testReceivePermissionConflictOnSet() {
        // pub 先设置 shared.key
        officer.receive(buildManifest(0, new PropertySetOperation("shared.key", "v1")));

        // 另一个发布者 pub2 尝试设置 shared.key
        DefaultPropertyPublisher pub2 = DefaultPropertyPublisher.builder().name("pub2").build();
        Map<PropertyPublisher, Integer> pubs = new ConcurrentHashMap<>();
        pubs.put(publisher, 0); // publisher 已被处理
        pubs.put(pub2, 0);
        DefaultPropertyOfficer officerWithPub2 = DefaultPropertyOfficer.builder()
                .name("officer2").publishers(pubs).build();

        // 用 publisher 先设置
        officerWithPub2.receive(buildManifest(publisher, 0, new PropertySetOperation("shared.key", "v1")));
        // 用 pub2 尝试覆盖
        PublisherManifestResult result = officerWithPub2.receive(
                buildManifest(pub2, 0, new PropertySetOperation("shared.key", "v2")));

        assertEquals(PublisherManifestResult.Status.PARTIAL_SUCCESS, result.status());
        officerWithPub2.close();
        if (!pub2.isClosed()) pub2.close();
    }

    @Test
    @DisplayName("不同发布者试图删除他人属性时返回 PARTIAL_SUCCESS（权限冲突）")
    void testReceivePermissionConflictOnRemove() {
        // pub 设置 owned.key
        officer.receive(buildManifest(0, new PropertySetOperation("owned.key", "v")));

        // 另一个发布者尝试删除 owned.key
        DefaultPropertyPublisher pub2 = DefaultPropertyPublisher.builder().name("pub2").build();
        Map<PropertyPublisher, Integer> pubs = new ConcurrentHashMap<>();
        pubs.put(publisher, 0);
        pubs.put(pub2, 0);
        DefaultPropertyOfficer sharedOfficer = DefaultPropertyOfficer.builder()
                .name("shared").publishers(pubs).build();

        sharedOfficer.receive(buildManifest(publisher, 0, new PropertySetOperation("owned.key", "v")));
        PublisherManifestResult result = sharedOfficer.receive(
                buildManifest(pub2, 0, new PropertyRemoveOperation("owned.key")));

        assertEquals(PublisherManifestResult.Status.PARTIAL_SUCCESS, result.status());
        sharedOfficer.close();
        if (!pub2.isClosed()) pub2.close();
    }

    // ===================== receiveAsync =====================

    @Test
    @DisplayName("receiveAsync 关闭后返回已完成的 ERROR Future")
    void testReceiveAsyncAfterClosed() throws Exception {
        officer.close();
        PublisherManifest manifest = buildManifest(0, new PropertySetOperation("k", "v"));

        CompletableFuture<PublisherManifestResult> future = officer.receiveAsync(manifest);

        assertTrue(future.isDone());
        assertEquals(PublisherManifestResult.Status.ERROR, future.get().status());
    }

    @Test
    @DisplayName("receiveAsync 未知发布者返回已完成的 ERROR Future")
    void testReceiveAsyncUnknownPublisher() throws Exception {
        DefaultPropertyPublisher unknownPub = DefaultPropertyPublisher.builder().name("unknown").build();
        PublisherManifest manifest = buildManifest(unknownPub, 0, new PropertySetOperation("k", "v"));

        CompletableFuture<PublisherManifestResult> future = officer.receiveAsync(manifest);

        assertTrue(future.isDone());
        assertEquals(PublisherManifestResult.Status.ERROR, future.get().status());
        unknownPub.close();
    }

    @Test
    @DisplayName("receiveAsync 版本过旧返回已完成的 SKIP Future")
    void testReceiveAsyncOldVersion() throws Exception {
        officer.receive(buildManifest(0, new PropertySetOperation("k", "v")));

        CompletableFuture<PublisherManifestResult> future =
                officer.receiveAsync(buildManifest(0, new PropertySetOperation("k2", "v2")));

        assertTrue(future.isDone());
        assertEquals(PublisherManifestResult.Status.SKIP, future.get().status());
    }

    // ===================== offPublisher =====================

    @Test
    @DisplayName("offPublisher 移除发布者发布的所有属性")
    void testOffPublisherRemovesProperties() {
        officer.receive(buildManifest(0,
                new PropertySetOperation("k1", "v1"),
                new PropertySetOperation("k2", "v2")));

        RecordingSubscription subscription = new RecordingSubscription("sub", new AnyPropertySelector());
        officer.subscribe(subscription);
        subscription.receivedOperations.clear(); // 清空初始化通知

        officer.offPublisher(publisher);

        // 验证订阅者收到了两个 remove 操作
        assertEquals(2, subscription.receivedOperations.size());
        assertTrue(subscription.receivedOperations.stream()
                .allMatch(op -> op instanceof PropertyRemoveOperation));
    }

    @Test
    @DisplayName("offPublisher 后 Officer 版本递增")
    void testOffPublisherIncrementsVersion() {
        int versionBefore = officer.currentVersion();
        officer.offPublisher(publisher);
        assertEquals(versionBefore + 1, officer.currentVersion());
    }

    @Test
    @DisplayName("Officer 关闭后 offPublisher 静默忽略")
    void testOffPublisherAfterClosed() {
        officer.close();
        assertDoesNotThrow(() -> officer.offPublisher(publisher));
    }

    // ===================== subscribe / unsubscribe =====================

    @Test
    @DisplayName("subscribe 触发初始化通知：发送已有属性")
    void testSubscribeInitialNotification() {
        officer.receive(buildManifest(0,
                new PropertySetOperation("k1", "v1"),
                new PropertySetOperation("k2", "v2")));

        RecordingSubscription subscription = new RecordingSubscription("sub", new AnyPropertySelector());
        officer.subscribe(subscription);

        assertEquals(2, subscription.receivedOperations.size());
        assertTrue(subscription.receivedOperations.stream().allMatch(op -> op instanceof PropertySetOperation));
    }

    @Test
    @DisplayName("subscribe 无已有属性时不发初始化通知")
    void testSubscribeNoInitialNotification() {
        RecordingSubscription subscription = new RecordingSubscription("sub", new AnyPropertySelector());
        officer.subscribe(subscription);

        assertTrue(subscription.receivedOperations.isEmpty());
    }

    @Test
    @DisplayName("subscribe 使用 ExactSelector 时只收到匹配属性的初始化通知")
    void testSubscribeWithExactSelector() {
        officer.receive(buildManifest(0,
                new PropertySetOperation("db.host", "localhost"),
                new PropertySetOperation("app.name", "test")));

        RecordingSubscription subscription = new RecordingSubscription("sub",
                key -> key.startsWith("db."));
        officer.subscribe(subscription);

        assertEquals(1, subscription.receivedOperations.size());
        PropertySetOperation op = (PropertySetOperation) subscription.receivedOperations.get(0);
        assertEquals("db.host", op.key());
    }

    @Test
    @DisplayName("unsubscribe 后不再接收通知")
    void testUnsubscribeStopsNotifications() {
        RecordingSubscription subscription = new RecordingSubscription("sub", new AnyPropertySelector());
        officer.subscribe(subscription);
        officer.unsubscribe(subscription);
        subscription.receivedOperations.clear();

        officer.receive(buildManifest(0, new PropertySetOperation("k", "v")));

        assertTrue(subscription.receivedOperations.isEmpty());
    }

    @Test
    @DisplayName("Officer 关闭后 subscribe 静默忽略")
    void testSubscribeAfterClosed() {
        officer.close();
        RecordingSubscription subscription = new RecordingSubscription("sub", new AnyPropertySelector());
        assertDoesNotThrow(() -> officer.subscribe(subscription));
    }

    @Test
    @DisplayName("Officer 关闭后 unsubscribe 静默忽略")
    void testUnsubscribeAfterClosed() {
        RecordingSubscription subscription = new RecordingSubscription("sub", new AnyPropertySelector());
        officer.subscribe(subscription);
        officer.close();
        assertDoesNotThrow(() -> officer.unsubscribe(subscription));
    }

    // ===================== 订阅者通知 =====================

    @Test
    @DisplayName("receive 后通知订阅者成功的操作")
    void testSubscriberNotifiedAfterReceive() {
        RecordingSubscription subscription = new RecordingSubscription("sub", new AnyPropertySelector());
        officer.subscribe(subscription);
        subscription.receivedOperations.clear();

        officer.receive(buildManifest(0, new PropertySetOperation("key", "value")));

        assertEquals(1, subscription.receivedOperations.size());
        assertEquals("key", ((PropertySetOperation) subscription.receivedOperations.get(0)).key());
    }

    @Test
    @DisplayName("receive 失败操作不通知订阅者")
    void testFailedOperationNotNotified() {
        RecordingSubscription subscription = new RecordingSubscription("sub", new AnyPropertySelector());
        officer.subscribe(subscription);
        subscription.receivedOperations.clear();

        // remove 不存在的键，操作失败
        officer.receive(buildManifest(0, new PropertyRemoveOperation("nonexistent")));

        assertTrue(subscription.receivedOperations.isEmpty());
    }

    // ===================== 关闭行为 =====================

    @Test
    @DisplayName("close 后 isClosed 返回 true")
    void testCloseSetsClosed() {
        officer.close();
        assertTrue(officer.isClosed());
    }

    @Test
    @DisplayName("close 幂等")
    void testCloseIdempotent() {
        assertDoesNotThrow(() -> {
            officer.close();
            officer.close();
        });
    }

    // ===================== 辅助方法 =====================

    private PublisherManifest buildManifest(int version, PropertyOperation... operations) {
        return new PublisherManifest(publisher, version, List.of(operations), Instant.now());
    }

    private PublisherManifest buildManifest(PropertyPublisher pub, int version, PropertyOperation... operations) {
        return new PublisherManifest(pub, version, List.of(operations), Instant.now());
    }

    /**
     * 记录收到的操作的订阅客户端测试桩
     */
    static class RecordingSubscription implements PropertySubscription {
        private final String name;
        private final PropertySelector selector;
        final List<PropertyOperation> receivedOperations = new ArrayList<>();

        RecordingSubscription(String name, PropertySelector selector) {
            this.name = name;
            this.selector = selector;
        }

        @Override public String name() { return name; }
        @Override public PropertySelector selector() { return selector; }

        @Override
        public void dispatch(PropertyOperation... operations) {
            receivedOperations.addAll(Arrays.asList(operations));
        }

        @Override public void offOfficer(org.moper.cap.property.officer.PropertyOfficer officer) {}
        @Override public boolean isClosed() { return false; }
        @Override public void close() {}
    }
}
