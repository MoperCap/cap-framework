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
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * cap-example Web MVC 集成测试，验证应用启动和路由注册。
 */
@Slf4j
public class ExampleApplicationTest {

    /**
     * 测试 Web MVC Bean 的扫描和注册。
     */
    @Test
    void testWebMvcBeanRegistration() throws Exception {
        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, "--server.port=0").run()) {
            assertTrue(context.containsBean("userService"), "userService 应已注册");
            assertTrue(context.containsBean("productService"), "productService 应已注册");
            assertTrue(context.containsBean("orderService"), "orderService 应已注册");
            assertTrue(context.containsBean("userController"), "userController 应已注册");
            assertTrue(context.containsBean("productController"), "productController 应已注册");
            assertTrue(context.containsBean("orderController"), "orderController 应已注册");
        }
    }

    /**
     * 测试依赖注入是否正常工作。
     */
    @Test
    void testDependencyInjection() throws Exception {
        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, "--server.port=0").run()) {
            UserService userService = context.getBean("userService", UserService.class);
            assertNotNull(userService, "userService 不应为空");
            assertFalse(userService.getAllUsers().isEmpty(), "userService 应有初始数据");

            ProductService productService = context.getBean("productService", ProductService.class);
            assertNotNull(productService, "productService 不应为空");
            assertFalse(productService.getAllProducts().isEmpty(), "productService 应有初始数据");
        }
    }

    /**
     * 测试路由注册是否正常。
     */
    @Test
    void testRouteRegistration() throws Exception {
        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, "--server.port=0").run()) {
            RouteRegistry routeRegistry = context.getBean("routeRegistry", RouteRegistry.class);
            assertNotNull(routeRegistry, "routeRegistry 应已注册");
            assertFalse(routeRegistry.getAllRoutes().isEmpty(), "应至少注册一个路由");
            log.info("已注册路由数量: {}", routeRegistry.getAllRoutes().size());
        }
    }

    /**
     * 测试 DataSource 是否正确配置。
     */
    @Test
    void testDataSourceConfiguration() throws Exception {
        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, "--server.port=0").run()) {
            assertTrue(context.containsBean("dataSource"), "dataSource 应已注册");

            DataSource dataSource = context.getBean("dataSource", DataSource.class);
            assertNotNull(dataSource, "dataSource 不应为空");

            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                assertTrue(rs.next(), "users 表应有数据");
                int count = rs.getInt(1);
                assertTrue(count > 0, "users 表应有初始数据，实际行数: " + count);
                log.info("DataSource 连接测试成功，users 表行数: {}", count);
            }
        }
    }

    /**
     * 测试事务模块是否正确初始化。
     */
    @Test
    void testTransactionModuleInitialization() throws Exception {
        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, "--server.port=0").run()) {
            OrderService orderService = context.getBean("orderService", OrderService.class);
            assertNotNull(orderService, "orderService 不应为空");
            log.info("事务模块初始化成功，OrderService 已就绪");
        }
    }
}
