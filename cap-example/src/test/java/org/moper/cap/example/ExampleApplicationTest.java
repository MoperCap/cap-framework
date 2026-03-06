package org.moper.cap.example;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.moper.cap.boot.application.impl.DefaultCapApplication;
import org.moper.cap.core.context.RuntimeContext;
import org.moper.cap.example.service.OrderService;
import org.moper.cap.example.service.ProductService;
import org.moper.cap.example.service.UserService;
import org.moper.cap.web.router.RouteRegistry;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CAP Example 集成测试
 *
 * <p>测试内容：
 * <ol>
 *   <li>Web MVC Bean 的扫描和注册</li>
 *   <li>数据源配置和连接</li>
 *   <li>事务模块初始化</li>
 *   <li>依赖注入</li>
 *   <li>路由注册</li>
 * </ol>
 */
@Slf4j
public class ExampleApplicationTest {

    /**
     * 测试 Web MVC Bean 的扫描和注册。
     */
    @Test
    void testWebMvcBeanRegistration() throws Exception {
        log.info("\n========== 测试：Web MVC Bean 注册 ==========\n");

        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, "--server.port=0").run()) {
            assertTrue(context.containsBean("userService"), "userService 应已注册");
            assertTrue(context.containsBean("productService"), "productService 应已注册");
            assertTrue(context.containsBean("orderService"), "orderService 应已注册");

            log.info("✅ 所有服务 Bean 都已注册");
        }
    }

    /**
     * 测试依赖注入是否正常工作。
     */
    @Test
    void testDependencyInjection() throws Exception {
        log.info("\n========== 测试：依赖注入 ==========\n");

        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, "--server.port=0").run()) {
            UserService userService = context.getBean("userService", UserService.class);
            assertNotNull(userService, "userService 不应为空");

            ProductService productService = context.getBean("productService", ProductService.class);
            assertNotNull(productService, "productService 不应为空");

            OrderService orderService = context.getBean("orderService", OrderService.class);
            assertNotNull(orderService, "orderService 不应为空");

            log.info("✅ 依赖注入成功");
        }
    }

    /**
     * 测试路由注册是否正常。
     */
    @Test
    void testRouteRegistration() throws Exception {
        log.info("\n========== 测试：路由注册 ==========\n");

        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, "--server.port=0").run()) {
            RouteRegistry routeRegistry = context.getBean("routeRegistry", RouteRegistry.class);
            assertNotNull(routeRegistry, "routeRegistry 应已注册");
            assertFalse(routeRegistry.getAllRoutes().isEmpty(), "应至少注册一个路由");

            log.info("✅ 路由注册成功，已注册 {} 个路由", routeRegistry.getAllRoutes().size());
        }
    }

    /**
     * 测试 DataSource 是否正确配置和连接。
     */
    @Test
    void testDataSourceConfiguration() throws Exception {
        log.info("\n========== 测试：数据源配置 ==========\n");

        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, "--server.port=0").run()) {
            assertTrue(context.containsBean("dataSource"), "dataSource 应已注册");

            DataSource dataSource = context.getBean("dataSource", DataSource.class);
            assertNotNull(dataSource, "dataSource 不应为空");

            try (Connection conn = dataSource.getConnection()) {
                assertNotNull(conn);
                String dbProductName = conn.getMetaData().getDatabaseProductName();
                log.info("✅ 数据源连接成功: {}", dbProductName);
            }
        }
    }

    /**
     * 测试事务管理器初始化。
     */
    @Test
    void testTransactionManagerInitialization() throws Exception {
        log.info("\n========== 测试：事务管理器初始化 ==========\n");

        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, "--server.port=0").run()) {
            assertTrue(context.containsBean("transactionManager"), "transactionManager 应已注册");

            log.info("✅ 事务管理器初始化成功");
        }
    }

    /**
     * 综合测试：完整的应用启动流程。
     */
    @Test
    void testCompleteApplicationStartup() throws Exception {
        log.info("\n========== 综合测试：完整应用启动 ==========\n");

        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, "--server.port=0").run()) {
            assertTrue(context.containsBean("dataSource"));
            assertTrue(context.containsBean("transactionManager"));
            assertTrue(context.containsBean("transactionTemplate"));
            assertTrue(context.containsBean("userService"));
            assertTrue(context.containsBean("productService"));
            assertTrue(context.containsBean("orderService"));

            log.info("✅ 应用完整启动成功");
        }
    }
}

