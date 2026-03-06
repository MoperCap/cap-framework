package org.moper.cap.example;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.moper.cap.boot.application.impl.DefaultCapApplication;
import org.moper.cap.core.context.RuntimeContext;
import org.moper.cap.example.bean.PrototypeService;
import org.moper.cap.example.bean.SingletonService;
import org.moper.cap.example.model.AppConfig;
import org.moper.cap.example.model.DatabaseConnection;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IoC 高级功能集成测试
 *
 * <p>测试内容：
 * <ol>
 *   <li>工厂方法 Bean：验证 {@code AppBeanFactory} 的静态和非静态工厂方法创建的 Bean 正确注册</li>
 *   <li>initMethod 生命周期：验证 {@code DatabaseConnection} 经 {@code initMethod="connect"} 初始化后 {@code isConnected()} 为 true</li>
 *   <li>Bean scope：使用 {@code isSingleton()} / {@code isPrototype()} 验证 Bean 作用域</li>
 *   <li>Bean 类型查询：使用 {@code getBeansOfType()} 查询某类型的所有 Bean</li>
 * </ol>
 */
@Slf4j
public class IocAdvancedExampleTest {

    /**
     * 测试：工厂方法 Bean — 静态工厂方法创建的 DatabaseConnection Bean 已正确注册
     */
    @Test
    void testStaticFactoryMethodBean() throws Exception {
        log.info("\n========== 测试：静态工厂方法 Bean (databaseConnection) ==========\n");

        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, "--server.port=0").run()) {
            assertTrue(context.containsBean("databaseConnection"), "databaseConnection Bean 应已注册");

            DatabaseConnection dbConn = context.getBean("databaseConnection", DatabaseConnection.class);
            assertNotNull(dbConn, "databaseConnection Bean 不应为空");

            log.info("✅ 静态工厂方法 Bean 注册成功: url={}", dbConn.getUrl());
        }
    }

    /**
     * 测试：非静态工厂方法 Bean — AppConfig Bean 已正确注册
     */
    @Test
    void testInstanceFactoryMethodBean() throws Exception {
        log.info("\n========== 测试：非静态工厂方法 Bean (appConfig) ==========\n");

        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, "--server.port=0").run()) {
            assertTrue(context.containsBean("appConfig"), "appConfig Bean 应已注册");

            AppConfig appConfig = context.getBean("appConfig", AppConfig.class);
            assertNotNull(appConfig, "appConfig Bean 不应为空");
            assertEquals("cap-example", appConfig.getAppName(), "appName 应为 cap-example");
            assertEquals("1.0-SNAPSHOT", appConfig.getVersion(), "version 应为 1.0-SNAPSHOT");

            log.info("✅ 非静态工厂方法 Bean 注册成功: {}", appConfig);
        }
    }

    /**
     * 测试：initMethod 生命周期 — DatabaseConnection 经 initMethod="connect" 初始化后 isConnected() 为 true
     */
    @Test
    void testInitMethodLifecycle() throws Exception {
        log.info("\n========== 测试：initMethod 生命周期 (DatabaseConnection) ==========\n");

        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, "--server.port=0").run()) {
            DatabaseConnection dbConn = context.getBean("databaseConnection", DatabaseConnection.class);
            assertNotNull(dbConn, "databaseConnection Bean 不应为空");
            assertTrue(dbConn.isConnected(), "DatabaseConnection 经 initMethod=\"connect\" 初始化后应已连接");

            log.info("✅ initMethod 生命周期成功：isConnected={}", dbConn.isConnected());
        }
    }

    /**
     * 测试：Bean scope — 使用 isSingleton() / isPrototype() 验证 Bean 作用域
     */
    @Test
    void testBeanScope() throws Exception {
        log.info("\n========== 测试：Bean Scope (isSingleton / isPrototype) ==========\n");

        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, "--server.port=0").run()) {
            // singletonService 应为单例
            assertTrue(context.isSingleton("singletonService"), "singletonService 应为 SINGLETON 作用域");
            assertFalse(context.isPrototype("singletonService"), "singletonService 不应为 PROTOTYPE 作用域");

            // prototypeService 应为原型
            assertFalse(context.isSingleton("prototypeService"), "prototypeService 不应为 SINGLETON 作用域");
            assertTrue(context.isPrototype("prototypeService"), "prototypeService 应为 PROTOTYPE 作用域");

            log.info("✅ Bean scope 验证成功");
        }
    }

    /**
     * 测试：Bean 类型查询 — getBeansOfType() 查询指定类型的所有 Bean
     */
    @Test
    void testGetBeansOfType() throws Exception {
        log.info("\n========== 测试：getBeansOfType() Bean 类型查询 ==========\n");

        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, "--server.port=0").run()) {
            // 查询所有 SingletonService 类型的 Bean
            Map<String, SingletonService> singletonBeans = context.getBeansOfType(SingletonService.class);
            assertNotNull(singletonBeans, "getBeansOfType 结果不应为空");
            assertFalse(singletonBeans.isEmpty(), "应至少有一个 SingletonService Bean");
            assertTrue(singletonBeans.containsKey("singletonService"), "应包含 singletonService Bean");

            // 查询所有 PrototypeService 类型的 Bean
            Map<String, PrototypeService> prototypeBeans = context.getBeansOfType(PrototypeService.class);
            assertNotNull(prototypeBeans, "getBeansOfType 结果不应为空");
            assertFalse(prototypeBeans.isEmpty(), "应至少有一个 PrototypeService Bean");

            log.info("✅ getBeansOfType 查询成功：SingletonService={} 个，PrototypeService={} 个",
                    singletonBeans.size(), prototypeBeans.size());
        }
    }
}
