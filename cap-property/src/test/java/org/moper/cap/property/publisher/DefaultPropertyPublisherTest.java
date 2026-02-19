package org.moper.cap.property.publisher;

import org.junit.jupiter.api.*;
import org.moper.cap.property.event.PropertyRemoveOperation;
import org.moper.cap.property.event.PropertySetOperation;
import org.moper.cap.property.event.PublisherManifest;
import org.moper.cap.property.exception.PropertyException;
import org.moper.cap.property.exception.PropertyManifestVersionException;
import org.moper.cap.property.officer.PropertyOfficer;
import org.moper.cap.property.publisher.impl.DefaultPropertyPublisher;
import org.moper.cap.property.result.PublisherManifestResult;
import org.moper.cap.property.subscriber.PropertySubscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DefaultPropertyPublisher 测试")
class DefaultPropertyPublisherTest {

    private DefaultPropertyPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = DefaultPropertyPublisher.builder()
                .name("test-publisher")
                .build();
    }

    @AfterEach
    void tearDown() {
        if (!publisher.isClosed()) {
            publisher.close();
        }
    }

    // ===================== 基本属性 =====================

    @Test
    @DisplayName("正确返回 name")
    void testName() {
        assertEquals("test-publisher", publisher.name());
    }

    @Test
    @DisplayName("初始版本号为 0")
    void testInitialVersion() {
        assertEquals(0, publisher.currentVersion());
    }

    @Test
    @DisplayName("初始未关闭")
    void testInitialNotClosed() {
        assertFalse(publisher.isClosed());
    }

    @Test
    @DisplayName("初始没有签约的 Officer")
    void testInitialNoOfficers() {
        assertEquals(0, publisher.getOfficerCount());
        assertTrue(publisher.getOfficers().isEmpty());
    }

    // ===================== 签约与解约 =====================

    @Test
    @DisplayName("成功与 Officer 签约")
    void testContract() throws PropertyException {
        RecordingOfficer officer = new RecordingOfficer("o1");
        publisher.contract(officer);

        assertEquals(1, publisher.getOfficerCount());
        assertTrue(publisher.isContractOfficer(officer));
    }

    @Test
    @DisplayName("重复签约同一个 Officer 不增加数量")
    void testContractDuplicate() throws PropertyException {
        RecordingOfficer officer = new RecordingOfficer("o1");
        publisher.contract(officer);
        publisher.contract(officer);

        assertEquals(1, publisher.getOfficerCount());
    }

    @Test
    @DisplayName("成功与 Officer 解约")
    void testUncontract() throws PropertyException {
        RecordingOfficer officer = new RecordingOfficer("o1");
        publisher.contract(officer);
        publisher.uncontract(officer);

        assertEquals(0, publisher.getOfficerCount());
        assertFalse(publisher.isContractOfficer(officer));
        assertEquals(1, officer.offPublisherCount);
    }

    @Test
    @DisplayName("关闭后签约抛出异常")
    void testContractAfterClosedThrows() {
        publisher.close();
        RecordingOfficer officer = new RecordingOfficer("o1");

        assertThrows(PropertyException.class, () -> publisher.contract(officer));
    }

    @Test
    @DisplayName("关闭后解约静默忽略")
    void testUncontractAfterClosedIsIgnored() throws PropertyException {
        RecordingOfficer officer = new RecordingOfficer("o1");
        publisher.contract(officer);
        publisher.close();

        // close 已经调用了 offPublisher；再次 uncontract 应静默忽略
        int countBeforeUncontract = officer.offPublisherCount;
        assertDoesNotThrow(() -> publisher.uncontract(officer));
        assertEquals(countBeforeUncontract, officer.offPublisherCount,
                "关闭后 uncontract 不应再次调用 offPublisher");
    }

    @Test
    @DisplayName("getOfficers 返回不可变副本")
    void testGetOfficersReturnsImmutableCopy() throws PropertyException {
        RecordingOfficer officer = new RecordingOfficer("o1");
        publisher.contract(officer);

        assertThrows(UnsupportedOperationException.class,
                () -> publisher.getOfficers().clear());
    }

    // ===================== 发布（同步） =====================

    @Test
    @DisplayName("publish 后版本号递增")
    void testPublishIncrementsVersion() {
        publisher.publish(new PropertySetOperation("key", "value"));
        assertEquals(1, publisher.currentVersion());

        publisher.publish(new PropertySetOperation("key2", "value2"));
        assertEquals(2, publisher.currentVersion());
    }

    @Test
    @DisplayName("publish 将 manifest 发给所有签约的 Officer")
    void testPublishSendsToAllOfficers() throws PropertyException {
        RecordingOfficer officer1 = new RecordingOfficer("o1");
        RecordingOfficer officer2 = new RecordingOfficer("o2");
        publisher.contract(officer1);
        publisher.contract(officer2);

        publisher.publish(new PropertySetOperation("key", "value"));

        assertEquals(1, officer1.receivedManifests.size());
        assertEquals(1, officer2.receivedManifests.size());
    }

    @Test
    @DisplayName("publish 后可以 pull 对应版本")
    void testPublishAndPull() throws PropertyManifestVersionException {
        publisher.publish(new PropertySetOperation("key", "value"));
        PublisherManifest manifest = publisher.pull(0);

        assertNotNull(manifest);
        assertEquals(0, manifest.version());
        assertEquals(publisher, manifest.publisher());
        assertEquals(1, manifest.operations().size());
    }

    @Test
    @DisplayName("关闭后 publish 返回空列表并不递增版本")
    void testPublishAfterClosedReturnsEmpty() {
        publisher.close();

        List<PublisherManifestResult> results = publisher.publish(new PropertySetOperation("key", "value"));

        assertTrue(results.isEmpty());
        assertEquals(0, publisher.currentVersion());
    }

    @Test
    @DisplayName("publish 无 Officer 时返回空结果列表")
    void testPublishNoOfficers() {
        List<PublisherManifestResult> results = publisher.publish(new PropertySetOperation("k", "v"));
        assertTrue(results.isEmpty());
        assertEquals(1, publisher.currentVersion());
    }

    @Test
    @DisplayName("publish 包含多个操作类型")
    void testPublishMultipleOperations() throws PropertyManifestVersionException {
        publisher.publish(
                new PropertySetOperation("k1", "v1"),
                new PropertyRemoveOperation("k2")
        );

        PublisherManifest manifest = publisher.pull(0);
        assertEquals(2, manifest.operations().size());
    }

    // ===================== 发布（异步） =====================

    @Test
    @DisplayName("publishAsync 返回 CompletableFuture 列表")
    void testPublishAsync() throws PropertyException {
        RecordingOfficer officer = new RecordingOfficer("o1");
        publisher.contract(officer);

        List<CompletableFuture<PublisherManifestResult>> futures =
                publisher.publishAsync(new PropertySetOperation("key", "value"));

        assertFalse(futures.isEmpty());
        assertEquals(1, futures.size());
    }

    @Test
    @DisplayName("关闭后 publishAsync 返回空列表")
    void testPublishAsyncAfterClosedReturnsEmpty() {
        publisher.close();

        List<CompletableFuture<PublisherManifestResult>> futures =
                publisher.publishAsync(new PropertySetOperation("key", "value"));

        assertTrue(futures.isEmpty());
    }

    // ===================== pull =====================

    @Test
    @DisplayName("pull 负数版本抛出异常")
    void testPullNegativeVersionThrows() {
        publisher.publish(new PropertySetOperation("key", "value"));
        assertThrows(PropertyManifestVersionException.class, () -> publisher.pull(-1));
    }

    @Test
    @DisplayName("pull 超过当前版本抛出异常")
    void testPullExceedCurrentVersionThrows() {
        assertThrows(PropertyManifestVersionException.class, () -> publisher.pull(0));
    }

    @Test
    @DisplayName("pull 范围查询：[0, 2) 返回两个清单")
    void testPullRange() throws PropertyManifestVersionException {
        publisher.publish(new PropertySetOperation("k1", "v1"));
        publisher.publish(new PropertySetOperation("k2", "v2"));

        List<PublisherManifest> manifests = publisher.pull(0, 2);

        assertEquals(2, manifests.size());
        assertEquals(0, manifests.get(0).version());
        assertEquals(1, manifests.get(1).version());
    }

    @Test
    @DisplayName("pull 范围：beginVersionID < 0 抛异常")
    void testPullRangeNegativeBegin() {
        publisher.publish(new PropertySetOperation("k", "v"));
        assertThrows(PropertyManifestVersionException.class, () -> publisher.pull(-1, 1));
    }

    @Test
    @DisplayName("pull 范围：beginVersionID >= endVersionID 抛异常")
    void testPullRangeBeginGreaterThanEnd() {
        publisher.publish(new PropertySetOperation("k", "v"));
        assertThrows(PropertyManifestVersionException.class, () -> publisher.pull(1, 0));
    }

    @Test
    @DisplayName("pull 范围：endVersionID > currentVersion 抛异常")
    void testPullRangeEndExceedsCurrentVersion() {
        publisher.publish(new PropertySetOperation("k", "v"));
        assertThrows(PropertyManifestVersionException.class, () -> publisher.pull(0, 5));
    }

    // ===================== 关闭行为 =====================

    @Test
    @DisplayName("关闭后 isClosed 返回 true")
    void testCloseSetsClosed() {
        publisher.close();
        assertTrue(publisher.isClosed());
    }

    @Test
    @DisplayName("关闭幂等：多次关闭不报错")
    void testCloseIdempotent() {
        assertDoesNotThrow(() -> {
            publisher.close();
            publisher.close();
            publisher.close();
        });
    }

    @Test
    @DisplayName("关闭时通知所有签约 Officer")
    void testCloseNotifiesOfficers() throws PropertyException {
        RecordingOfficer officer1 = new RecordingOfficer("o1");
        RecordingOfficer officer2 = new RecordingOfficer("o2");
        publisher.contract(officer1);
        publisher.contract(officer2);

        publisher.close();

        assertEquals(1, officer1.offPublisherCount);
        assertEquals(1, officer2.offPublisherCount);
    }

    // ===================== 测试用 Officer stub =====================

    /**
     * 简单记录调用的 PropertyOfficer 测试桩
     */
    static class RecordingOfficer implements PropertyOfficer {
        private final String name;
        final List<PublisherManifest> receivedManifests = new ArrayList<>();
        int offPublisherCount = 0;

        RecordingOfficer(String name) {
            this.name = name;
        }

        @Override public String name() { return name; }
        @Override public int currentVersion() { return 0; }

        @Override
        public PublisherManifestResult receive(PublisherManifest manifest) {
            receivedManifests.add(manifest);
            return PublisherManifestResult.totalSuccess(this, manifest, List.of());
        }

        @Override
        public CompletableFuture<PublisherManifestResult> receiveAsync(PublisherManifest manifest) {
            receivedManifests.add(manifest);
            return CompletableFuture.completedFuture(
                    PublisherManifestResult.totalSuccess(this, manifest, List.of()));
        }

        @Override
        public void offPublisher(PropertyPublisher publisher) {
            offPublisherCount++;
        }

        @Override public void subscribe(PropertySubscription subscription) {}
        @Override public void unsubscribe(PropertySubscription subscription) {}
        @Override public boolean isClosed() { return false; }
        @Override public void close() {}
    }
}
