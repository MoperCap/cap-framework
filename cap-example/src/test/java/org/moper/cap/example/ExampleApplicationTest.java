package org.moper.cap.example;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.moper.cap.boot.application.impl.DefaultCapApplication;
import org.moper.cap.core.context.RuntimeContext;
import org.moper.cap.example.service.OrderService;
import org.moper.cap.example.service.ProductService;
import org.moper.cap.example.service.UserService;
import org.moper.cap.web.router.RouteRegistry;

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
}
