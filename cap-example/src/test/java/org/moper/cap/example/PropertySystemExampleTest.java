package org.moper.cap.example;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.moper.cap.boot.application.impl.DefaultCapApplication;
import org.moper.cap.core.context.RuntimeContext;
import org.moper.cap.example.config.AppPropertyConfig;
import org.moper.cap.example.config.DynamicPropertyWatcher;
import org.moper.cap.property.event.PropertyRemoveOperation;
import org.moper.cap.property.event.PropertySetOperation;
import org.moper.cap.property.officer.PropertyOfficer;
import org.moper.cap.property.publisher.PropertyPublisher;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 属性系统集成测试
 *
 * <p>测试内容：
 * <ol>
 *   <li>{@code @Value} 注入：验证从 {@code application.yaml} 读取的属性值正确注入</li>
 *   <li>{@code @Subscription} + {@code @Subscriber}：验证动态属性监听与回调触发</li>
 *   <li>{@link PropertyOfficer} 获取：验证从 {@link RuntimeContext} 获取 PropertyOfficer 正常</li>
 * </ol>
 */
@Slf4j
public class PropertySystemExampleTest {

    /**
     * 测试：@Value 注入 — 验证从 application.yaml 读取的属性值正确注入到 Bean 字段
     */
    @Test
    void testValueInjection() throws Exception {
        log.info("\n========== 测试：@Value 注入 ==========\n");

        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, "--server.port=0").run()) {
            AppPropertyConfig config = context.getBean("appPropertyConfig", AppPropertyConfig.class);
            assertNotNull(config, "appPropertyConfig Bean 不应为空");

            assertEquals("cap-example", config.getAppName(), "app.name 应为 cap-example");
            assertEquals("1.0.0", config.getAppVersion(), "app.version 应为 1.0.0");
            assertNotNull(config.getAppDescription(), "app.description 不应为空");

            log.info("✅ @Value 注入成功: appName={}, appVersion={}", config.getAppName(), config.getAppVersion());
        }
    }

    /**
     * 测试：PropertyOfficer 获取 — 验证从 RuntimeContext 获取 PropertyOfficer 正常
     */
    @Test
    void testPropertyOfficerRetrieval() throws Exception {
        log.info("\n========== 测试：PropertyOfficer 获取 ==========\n");

        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, "--server.port=0").run()) {
            PropertyOfficer officer = context.getPropertyOfficer();
            assertNotNull(officer, "PropertyOfficer 不应为空");

            log.info("✅ PropertyOfficer 获取成功: {}", officer.name());
        }
    }

    /**
     * 测试：@Subscription + @Subscriber — 验证发布属性变更后订阅者回调被正确触发
     */
    @Test
    void testSubscriptionAndSubscriber() throws Exception {
        log.info("\n========== 测试：@Subscription + @Subscriber 动态监听 ==========\n");

        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, "--server.port=0").run()) {
            PropertyOfficer officer = context.getPropertyOfficer();
            assertNotNull(officer, "PropertyOfficer 不应为空");

            PropertyPublisher publisher = officer.getPublisher("example-publisher");
            assertNotNull(publisher, "PropertyPublisher 不应为空");

            DynamicPropertyWatcher watcher = context.getBean("dynamicPropertyWatcher", DynamicPropertyWatcher.class);
            assertNotNull(watcher, "dynamicPropertyWatcher Bean 不应为空");

            // 发布属性设置事件，验证 onSet 回调被触发
            publisher.publish(new PropertySetOperation("app.dynamic.value", "hello-cap"));
            assertEquals("hello-cap", watcher.getDynamicValue(), "dynamicValue 应更新为 hello-cap");
            assertEquals("hello-cap", watcher.getLastSetValue(), "lastSetValue 应为 hello-cap");

            // 再次发布，验证值可以更新
            publisher.publish(new PropertySetOperation("app.dynamic.value", "world-cap"));
            assertEquals("world-cap", watcher.getDynamicValue(), "dynamicValue 应更新为 world-cap");

            // 发布属性删除事件，验证 onRemoved 回调被触发
            publisher.publish(new PropertyRemoveOperation("app.dynamic.value"));
            assertTrue(watcher.isRemovedCalled(), "removedCalled 应为 true");

            log.info("✅ 动态属性监听成功：onSet 和 onRemoved 回调均被正确触发");
        }
    }
}
