package org.moper.cap.property.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.moper.cap.property.event.PropertyOperation;
import org.moper.cap.property.event.PropertyRemoveOperation;
import org.moper.cap.property.event.PropertySetOperation;
import org.moper.cap.property.event.PublisherManifest;
import org.moper.cap.property.exception.PropertyException;
import org.moper.cap.property.exception.PropertyManifestVersionException;
import org.moper.cap.property.officer.PropertyOfficer;
import org.moper.cap.property.officer.impl.DefaultPropertyOfficer;
import org.moper.cap.property.publisher.PropertyPublisher;
import org.moper.cap.property.publisher.impl.DefaultPropertyPublisher;
import org.moper.cap.property.result.PublisherManifestResult;
import org.moper.cap.property.subscriber.PropertySubscriber;
import org.moper.cap.property.subscriber.PropertySubscription;
import org.moper.cap.property.subscriber.subcription.DefaultPropertySubscription;
import org.moper.cap.property.subscriber.view.DefaultPropertyViewPool;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 属性系统完整测试套件
 * <p>
 * 根据实际实现编写的测试用例，涵盖：
 * <ul>
 * <li>Publisher 发布者单元测试</li>
 * <li>Officer 属性官单元测试</li>
 * <li>Subscription 订阅客户端单元测试</li>
 * <li>Subscriber 属性订阅者单元测试</li>
 * <li>ViewPool 只读视图池单元测试</li>
 * <li>集成测试</li>
 * <li>并发测试</li>
 * <li>异常处理测试</li>
 * </ul>
 */
@DisplayName("属性系统完整测试套件")
public class PropertySystemTest {

    // ===================== Publisher 发布者测试 =====================

    @Nested
    @DisplayName("PropertyPublisher 发布者单元测试")
    class PublisherTests {

        private PropertyPublisher publisher;

        @BeforeEach
        void setUp() {
            publisher = DefaultPropertyPublisher.builder()
                    .name("TestPublisher")
                    .build();
        }

        @Test
        @DisplayName("发布者创建和基本信息")
        void testPublisherCreation() {
            assertNotNull(publisher);
            assertEquals("TestPublisher", publisher.name());
            assertEquals(0, publisher.currentVersion());
            assertFalse(publisher.isClosed());
        }

        @Test
        @DisplayName("同步发布单个属性操作")
        void testPublishSingleOperation() {
            PropertySetOperation operation = new PropertySetOperation("db.host", "localhost");
            List<PublisherManifestResult> results = publisher.publish(operation);

            assertNotNull(results);
            assertEquals(1, publisher.currentVersion());
        }

        @Test
        @DisplayName("同步发布多个属性操作")
        void testPublishMultipleOperations() {
            PropertySetOperation op1 = new PropertySetOperation("db.host", "localhost");
            PropertySetOperation op2 = new PropertySetOperation("db.port", 3306);
            PropertySetOperation op3 = new PropertySetOperation("db.user", "admin");

            List<PublisherManifestResult> results = publisher.publish(op1, op2, op3);

            assertNotNull(results);
            assertEquals(1, publisher.currentVersion());
        }

        @Test
        @DisplayName("发布者版本号正确递增")
        void testPublisherVersionIncrement() {
            assertEquals(0, publisher.currentVersion());

            publisher.publish(new PropertySetOperation("key1", "v1"));
            assertEquals(1, publisher.currentVersion());

            publisher.publish(new PropertySetOperation("key2", "v2"));
            assertEquals(2, publisher.currentVersion());
        }

        @Test
        @DisplayName("异步发布属性操作")
        void testPublishAsync() {
            PropertySetOperation operation = new PropertySetOperation("db.host", "localhost");
            List<CompletableFuture<PublisherManifestResult>> futures = publisher.publishAsync(operation);

            assertNotNull(futures);
            assertEquals(1, publisher.currentVersion());
        }

        @Test
        @DisplayName("拉取单个版本的事件清单")
        void testPullSingleVersion() throws PropertyManifestVersionException {
            publisher.publish(new PropertySetOperation("key1", "value1"));

            PublisherManifest manifest = publisher.pull(0);
            assertNotNull(manifest);
            assertEquals(0, manifest.version());
        }

        @Test
        @DisplayName("拉取版本范围的事件清单")
        void testPullVersionRange() throws PropertyManifestVersionException {
            publisher.publish(new PropertySetOperation("key1", "v1"));
            publisher.publish(new PropertySetOperation("key2", "v2"));
            publisher.publish(new PropertySetOperation("key3", "v3"));

            List<PublisherManifest> manifests = publisher.pull(0, 3);
            assertNotNull(manifests);
            assertEquals(3, manifests.size());
        }

        @Test
        @DisplayName("拉取无效版本抛出异常")
        void testPullInvalidVersion() {
            assertThrows(PropertyManifestVersionException.class, () -> publisher.pull(999));
        }

        @Test
        @DisplayName("与Officer签约")
        void testContractWithOfficer() throws PropertyException {
            PropertyOfficer officer = DefaultPropertyOfficer.builder()
                    .name("TestOfficer")
                    .build();

            publisher.contract(officer);

            assertEquals(1, publisher.getOfficerCount());
            assertTrue(publisher.isContractOfficer(officer));
        }

        @Test
        @DisplayName("与Officer解约")
        void testUncontractWithOfficer() throws PropertyException {
            PropertyOfficer officer = DefaultPropertyOfficer.builder()
                    .name("TestOfficer")
                    .build();

            publisher.contract(officer);
            publisher.uncontract(officer);

            assertEquals(0, publisher.getOfficerCount());
            assertFalse(publisher.isContractOfficer(officer));
        }

        @Test
        @DisplayName("获取已签约的Officer副本集合")
        void testGetOfficers() throws PropertyException {
            PropertyOfficer officer1 = DefaultPropertyOfficer.builder().name("Officer1").build();
            PropertyOfficer officer2 = DefaultPropertyOfficer.builder().name("Officer2").build();

            publisher.contract(officer1);
            publisher.contract(officer2);

            Set<PropertyOfficer> officers = publisher.getOfficers();
            assertEquals(2, officers.size());
            assertTrue(officers.contains(officer1));
            assertTrue(officers.contains(officer2));
        }

        @Test
        @DisplayName("发布者关闭")
        void testClosePublisher() {
            publisher.close();
            assertTrue(publisher.isClosed());
        }

        @Test
        @DisplayName("关闭后的发布者无法继续发布")
        void testPublishAfterClose() {
            publisher.close();
            assertTrue(publisher.isClosed());

            // 关闭后 publish 会返回空列表而不是抛出异常
            List<PublisherManifestResult> results = publisher.publish(new PropertySetOperation("key", "value"));
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("关闭后无法签约")
        void testContractAfterClose() {
            PropertyOfficer officer = DefaultPropertyOfficer.builder()
                    .name("TestOfficer")
                    .build();

            publisher.close();

            assertThrows(PropertyException.class, () -> publisher.contract(officer));
        }
    }

    // ===================== Officer 属性官测试 =====================

    @Nested
    @DisplayName("PropertyOfficer 属性官单元测试")
    class OfficerTests {

        private PropertyOfficer officer;
        private PropertyPublisher publisher;

        @BeforeEach
        void setUp() {
            officer = DefaultPropertyOfficer.builder()
                    .name("TestOfficer")
                    .build();
            publisher = DefaultPropertyPublisher.builder()
                    .name("TestPublisher")
                    .build();
        }

        @Test
        @DisplayName("Officer 创建和基本信息")
        void testOfficerCreation() {
            assertNotNull(officer);
            assertEquals("TestOfficer", officer.name());
            assertEquals(0, officer.currentVersion());
            assertFalse(officer.isClosed());
        }

        @Test
        @DisplayName("同步接收单个属性操作")
        void testReceiveSingleOperation() {
            PropertySetOperation operation = new PropertySetOperation("db.host", "localhost");
            List<PublisherManifestResult> pubResults = publisher.publish(operation);

            assertNotNull(pubResults);
            assertEquals(1, publisher.currentVersion());
        }

        @Test
        @DisplayName("异步接收属性操作")
        void testReceiveAsync() {
            PropertySetOperation operation = new PropertySetOperation("db.host", "localhost");
            List<CompletableFuture<PublisherManifestResult>> futures = publisher.publishAsync(operation);

            assertNotNull(futures);
        }

        @Test
        @DisplayName("Officer 订阅 PropertySubscription")
        void testSubscribePropertySubscription() {
            List<PropertySubscriber> subscribers = new ArrayList<>();
            subscribers.add(new TestPropertySubscriber("db.host"));

            PropertySubscription subscription = new DefaultPropertySubscription(
                    "TestSubscription",
                    subscribers
            );

            officer.subscribe(subscription);
            // 应该不抛出异常
        }

        @Test
        @DisplayName("Officer 取消订阅")
        void testUnsubscribe() {
            List<PropertySubscriber> subscribers = new ArrayList<>();
            subscribers.add(new TestPropertySubscriber("db.host"));

            PropertySubscription subscription = new DefaultPropertySubscription(
                    "TestSubscription",
                    subscribers
            );

            officer.subscribe(subscription);
            officer.unsubscribe(subscription);
            // 应该不抛出异常
        }

        @Test
        @DisplayName("Officer 处理发布者离线")
        void testOffPublisher() {
            PropertySetOperation operation = new PropertySetOperation("db.host", "localhost");
            publisher.publish(operation);

            officer.offPublisher(publisher);

            // 应该正确处理，不抛出异常
        }

        @Test
        @DisplayName("Officer 关闭")
        void testCloseOfficer() {
            officer.close();
            assertTrue(officer.isClosed());
        }

        @Test
        @DisplayName("Officer 重复关闭是幂等的")
        void testCloseIdempotent() {
            officer.close();
            officer.close();  // 应该不抛出异常
            assertTrue(officer.isClosed());
        }
    }

    // ===================== Subscription 订阅客户端测试 =====================

    @Nested
    @DisplayName("PropertySubscription 订阅客户端单元测试")
    class SubscriptionTests {

        private PropertySubscription subscription;
        private AtomicInteger callbackCount;

        @BeforeEach
        void setUp() {
            callbackCount = new AtomicInteger(0);

            List<PropertySubscriber> subscribers = new ArrayList<>();
            subscribers.add(new TestPropertySubscriber("db.host", callbackCount));
            subscribers.add(new TestPropertySubscriber("db.port", callbackCount));

            subscription = new DefaultPropertySubscription(
                    "TestSubscription",
                    subscribers
            );
        }

        @Test
        @DisplayName("Subscription 创建和基本信息")
        void testSubscriptionCreation() {
            assertNotNull(subscription);
            assertEquals("TestSubscription", subscription.name());
            assertNotNull(subscription.selector());
            assertFalse(subscription.isClosed());
        }

        @Test
        @DisplayName("Subscription 分发属性设置操作")
        void testDispatchSetOperation() {
            callbackCount.set(0);

            subscription.dispatch(
                    new PropertySetOperation("db.host", "localhost"),
                    new PropertySetOperation("db.port", 3306)
            );

            assertEquals(2, callbackCount.get());
        }

        @Test
        @DisplayName("Subscription 分发属性移除操作")
        void testDispatchRemoveOperation() {
            callbackCount.set(0);

            subscription.dispatch(
                    new PropertyRemoveOperation("db.host"),
                    new PropertyRemoveOperation("db.port")
            );

            assertEquals(2, callbackCount.get());
        }

        @Test
        @DisplayName("Subscription 只分发关心的属性")
        void testDispatchFilterBySubscriber() {
            callbackCount.set(0);

            subscription.dispatch(
                    new PropertySetOperation("db.host", "localhost"),
                    new PropertySetOperation("db.user", "admin")  // 不关心
            );

            assertEquals(1, callbackCount.get());  // 只有 db.host 的订阅者被调用
        }

        @Test
        @DisplayName("Subscription 关闭后忽略后续操作")
        void testDispatchAfterClose() {
            subscription.close();
            assertTrue(subscription.isClosed());

            callbackCount.set(0);
            subscription.dispatch(new PropertySetOperation("db.host", "localhost"));

            assertEquals(0, callbackCount.get());
        }

        @Test
        @DisplayName("Subscription 发布者离线通知")
        void testOffOfficer() {
            callbackCount.set(0);

            PropertyOfficer officer = DefaultPropertyOfficer.builder()
                    .name("TestOfficer")
                    .build();

            subscription.offOfficer(officer);

            assertEquals(2, callbackCount.get());  // 所有订阅者都被通知
        }

        @Test
        @DisplayName("Subscription 重复关闭是幂等的")
        void testCloseIdempotent() {
            subscription.close();
            subscription.close();  // 应该不抛出异常
            assertTrue(subscription.isClosed());
        }
    }

    // ===================== ViewPool 只读视图池测试 =====================

    @Nested
    @DisplayName("PropertyViewPool 只读视图池单元测试")
    class ViewPoolTests {

        private DefaultPropertyViewPool viewPool;

        @BeforeEach
        void setUp() {
            viewPool = new DefaultPropertyViewPool("TestViewPool");
        }

        @Test
        @DisplayName("ViewPool 创建和基本信息")
        void testViewPoolCreation() {
            assertNotNull(viewPool);
            assertEquals("TestViewPool", viewPool.name());
            assertNotNull(viewPool.selector());
            assertFalse(viewPool.isClosed());
        }

        @Test
        @DisplayName("ViewPool 获取原始属性值")
        void testGetRawPropertyValue() {
            viewPool.dispatch(new PropertySetOperation("key1", "value1"));

            Object value = viewPool.getRawPropertyValue("key1");
            assertEquals("value1", value);
        }

        @Test
        @DisplayName("ViewPool 类型转换 - String")
        void testGetPropertyValueAsString() {
            viewPool.dispatch(new PropertySetOperation("key1", "value1"));

            String value = viewPool.getPropertyValue("key1", String.class);
            assertEquals("value1", value);
        }

        @Test
        @DisplayName("ViewPool 类型转换 - Integer")
        void testGetPropertyValueAsInteger() {
            viewPool.dispatch(new PropertySetOperation("db.port", 3306));

            Integer value = viewPool.getPropertyValue("db.port", Integer.class);
            assertEquals(3306, value);
        }

        @Test
        @DisplayName("ViewPool 获取不存在的属性返回 null")
        void testGetNonExistentProperty() {
            assertNull(viewPool.getRawPropertyValue("nonexistent"));
            assertNull(viewPool.getPropertyValue("nonexistent", String.class));
        }

        @Test
        @DisplayName("ViewPool 获取或默认值")
        void testGetPropertyValueOrDefault() {
            viewPool.dispatch(new PropertySetOperation("key1", "value1"));

            String existValue = viewPool.getPropertyValueOrDefault("key1", String.class, "default");
            assertEquals("value1", existValue);

            String defaultValue = viewPool.getPropertyValueOrDefault("key2", String.class, "default");
            assertEquals("default", defaultValue);
        }

        @Test
        @DisplayName("ViewPool Optional 接口")
        void testGetPropertyValueOptional() {
            viewPool.dispatch(new PropertySetOperation("key1", "value1"));

            Optional<String> value = viewPool.getPropertyValueOptional("key1", String.class);
            assertTrue(value.isPresent());
            assertEquals("value1", value.get());

            Optional<String> empty = viewPool.getPropertyValueOptional("key2", String.class);
            assertFalse(empty.isPresent());
        }

        @Test
        @DisplayName("ViewPool 检查属性存在")
        void testContainsProperty() {
            viewPool.dispatch(new PropertySetOperation("key1", "value1"));

            assertTrue(viewPool.containsProperty("key1"));
            assertFalse(viewPool.containsProperty("key2"));
        }

        @Test
        @DisplayName("ViewPool 获取所有属性键")
        void testKeySet() {
            viewPool.dispatch(
                    new PropertySetOperation("key1", "value1"),
                    new PropertySetOperation("key2", "value2"),
                    new PropertySetOperation("key3", "value3")
            );

            Set<String> keys = viewPool.keySet();
            assertEquals(3, keys.size());
            assertTrue(keys.contains("key1"));
            assertTrue(keys.contains("key2"));
            assertTrue(keys.contains("key3"));
        }

        @Test
        @DisplayName("ViewPool 属性移除")
        void testPropertyRemoval() {
            viewPool.dispatch(new PropertySetOperation("key1", "value1"));
            assertTrue(viewPool.containsProperty("key1"));

            viewPool.dispatch(new PropertyRemoveOperation("key1"));
            assertFalse(viewPool.containsProperty("key1"));
        }

        @Test
        @DisplayName("ViewPool 属性覆盖更新")
        void testPropertyUpdate() {
            viewPool.dispatch(new PropertySetOperation("key1", "value1"));
            assertEquals("value1", viewPool.getRawPropertyValue("key1"));

            viewPool.dispatch(new PropertySetOperation("key1", "value2"));
            assertEquals("value2", viewPool.getRawPropertyValue("key1"));
        }

        @Test
        @DisplayName("ViewPool 关闭后继续可查询")
        void testQueryAfterClose() {
            viewPool.dispatch(new PropertySetOperation("key1", "value1"));
            viewPool.close();

            assertTrue(viewPool.isClosed());
            assertEquals("value1", viewPool.getRawPropertyValue("key1"));
        }

        @Test
        @DisplayName("ViewPool 关闭后不接收更新")
        void testNoUpdateAfterClose() {
            viewPool.dispatch(new PropertySetOperation("key1", "value1"));
            viewPool.close();

            viewPool.dispatch(new PropertySetOperation("key2", "value2"));
            assertFalse(viewPool.containsProperty("key2"));
        }
    }

    // ===================== 集成测试 =====================

    @Nested
    @DisplayName("属性系统集成测试")
    class IntegrationTests {

        private PropertyPublisher publisher;
        private PropertyOfficer officer;
        private PropertySubscription subscription;
        private DefaultPropertyViewPool viewPool;

        @BeforeEach
        void setUp() {
            publisher = DefaultPropertyPublisher.builder()
                    .name("IntegrationPublisher")
                    .build();

            officer = DefaultPropertyOfficer.builder()
                    .name("IntegrationOfficer")
                    .build();

            AtomicInteger callbackCount = new AtomicInteger(0);

            List<PropertySubscriber> subscribers = new ArrayList<>();
            subscribers.add(new TestPropertySubscriber("db.host", callbackCount));
            subscribers.add(new TestPropertySubscriber("db.port", callbackCount));

            subscription = new DefaultPropertySubscription(
                    "IntegrationSubscription",
                    subscribers
            );

            viewPool = new DefaultPropertyViewPool("IntegrationViewPool");
        }

        @Test
        @DisplayName("完整流程：发布 -> 订阅 -> 分发")
        void testCompleteFlow() throws PropertyException {
            // 发布属性
            publisher.publish(
                    new PropertySetOperation("db.host", "localhost"),
                    new PropertySetOperation("db.port", 3306)
            );

            // 注册订阅
            officer.subscribe(subscription);
            officer.subscribe(viewPool);

            // 分发给订阅者
            PublisherManifest manifest = publisher.pull(0);
            if (manifest != null) {
                subscription.dispatch(manifest.operations().toArray(new PropertyOperation[0]));
                viewPool.dispatch(manifest.operations().toArray(new PropertyOperation[0]));
            }
        }

        @Test
        @DisplayName("发布者签约 Officer")
        void testPublisherContractOfficer() throws PropertyException {
            publisher.contract(officer);

            assertTrue(publisher.isContractOfficer(officer));
            assertEquals(1, publisher.getOfficerCount());
        }

        @Test
        @DisplayName("多订阅者场景")
        void testMultipleSubscriptions() {
            officer.subscribe(subscription);
            officer.subscribe(viewPool);

            // 应该能正常注册两个订阅
            assertDoesNotThrow(() -> {
                List<PropertySetOperation> ops = new ArrayList<>();
                ops.add(new PropertySetOperation("db.host", "localhost"));
                ops.add(new PropertySetOperation("db.port", 3306));

                subscription.dispatch(ops.toArray(new PropertyOperation[0]));
                viewPool.dispatch(ops.toArray(new PropertyOperation[0]));
            });
        }
    }

    // ===================== 并发测试 =====================

    @Nested
    @DisplayName("并发场景测试")
    class ConcurrencyTests {

        private PropertyPublisher publisher;
        private DefaultPropertyViewPool viewPool;

        @BeforeEach
        void setUp() {
            publisher = DefaultPropertyPublisher.builder()
                    .name("ConcurrencyPublisher")
                    .build();

            viewPool = new DefaultPropertyViewPool("ConcurrencyViewPool");
        }

        @Test
        @DisplayName("并发发布属性")
        void testConcurrentPublish() throws InterruptedException {
            int threadCount = 5;
            int operationsPerThread = 20;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < operationsPerThread; j++) {
                            publisher.publish(
                                    new PropertySetOperation("key_" + threadId + "_" + j, "value")
                            );
                        }
                    } catch (Exception e) {
                        // Log exception instead of printStackTrace
                        System.err.println("Exception in publish: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }

            assertTrue(latch.await(30, TimeUnit.SECONDS));
            assertEquals(threadCount * operationsPerThread, publisher.currentVersion());
            executor.shutdown();
        }

        @Test
        @DisplayName("并发分发和查询")
        void testConcurrentDispatchAndQuery() throws InterruptedException {
            // 先分发一些属性
            for (int i = 0; i < 50; i++) {
                viewPool.dispatch(new PropertySetOperation("key_" + i, "value_" + i));
            }

            // 并发查询
            int threadCount = 5;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < 50; j++) {
                            String value = viewPool.getPropertyValue("key_" + j, String.class);
                            assertEquals("value_" + j, value);
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            assertTrue(latch.await(30, TimeUnit.SECONDS));
            executor.shutdown();
        }
    }

    // ===================== 异常处理测试 =====================

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Subscriber 异常不中断其他订阅者")
        void testSubscriberExceptionDoesNotInterrupt() {
            AtomicInteger callCount = new AtomicInteger(0);
            AtomicInteger exceptionCount = new AtomicInteger(0);

            List<PropertySubscriber> subscribers = new ArrayList<>();
            // 第一个订阅者抛出异常
            subscribers.add(new TestPropertySubscriber("key1", callCount) {
                @Override
                public void onSet(Object value) {
                    exceptionCount.incrementAndGet();
                    throw new RuntimeException("Test exception");
                }
            });
            // 第二个订阅者正常执行
            subscribers.add(new TestPropertySubscriber("key2", callCount));

            PropertySubscription subscription = new DefaultPropertySubscription(
                    "TestSub",
                    subscribers
            );

            // 临时重定向 System.err 以避免混淆的日志输出
            java.io.PrintStream originalErr = System.err;
            java.io.ByteArrayOutputStream errContent = new java.io.ByteArrayOutputStream();
            System.setErr(new java.io.PrintStream(errContent));

            try {
                // 应该不抛出异常（异常被内部捕获）
                assertDoesNotThrow(() ->
                        subscription.dispatch(
                                new PropertySetOperation("key1", "v1"),
                                new PropertySetOperation("key2", "v2")
                        )
                );

                // 验证：第一个订阅者被调用但抛出异常
                assertEquals(1, exceptionCount.get(), "第一个订阅者应该被调用");
                // 验证：第二个订阅者仍然被正常调用
                assertEquals(1, callCount.get(), "第二个订阅者应该被正常调用");

                // 验证：异常日志被记录
                String errorLog = errContent.toString();
                assertTrue(errorLog.contains("TestSub"), "应该记录订阅客户端名称");
                assertTrue(errorLog.contains("key1"), "应该记录订阅者键");
                assertTrue(errorLog.contains("Test exception"), "应该记录异常消息");
            } finally {
                // 恢复 System.err
                System.setErr(originalErr);
            }
        }

        @Test
        @DisplayName("ViewPool 查询空属性键抛出异常")
        void testViewPoolEmptyKey() {
            DefaultPropertyViewPool viewPool = new DefaultPropertyViewPool("TestViewPool");

            assertThrows(IllegalArgumentException.class, () -> viewPool.getRawPropertyValue(""));
        }
    }

    // ===================== 测试辅助类 =====================

    /**
     * 测试用 PropertySubscriber 实现
     */
    static class TestPropertySubscriber implements PropertySubscriber {

        private final String key;
        private final AtomicInteger callbackCount;

        TestPropertySubscriber(String key) {
            this.key = key;
            this.callbackCount = new AtomicInteger(0);
        }

        TestPropertySubscriber(String key, AtomicInteger callbackCount) {
            this.key = key;
            this.callbackCount = callbackCount;
        }

        @Override
        public String getPropertyKey() {
            return key;
        }

        @Override
        public void onSet(Object value) {
            callbackCount.incrementAndGet();
        }

        @Override
        public void onRemoved() {
            callbackCount.incrementAndGet();
        }
    }
}