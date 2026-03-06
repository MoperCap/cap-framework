package org.moper.cap.example;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.moper.cap.boot.application.impl.DefaultCapApplication;
import org.moper.cap.core.context.RuntimeContext;
import org.moper.cap.example.controller.OrderController;
import org.moper.cap.example.controller.ProductController;
import org.moper.cap.example.controller.UserController;
import org.moper.cap.web.http.HttpMethod;
import org.moper.cap.web.router.RouteDefinition;
import org.moper.cap.web.router.RouteRegistry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Web MVC 路由详细测试
 *
 * <p>测试内容：
 * <ol>
 *   <li>路由路径验证：检查 {@link RouteRegistry} 中注册的路由路径是否包含预期值</li>
 *   <li>HTTP 方法验证：验证路由的 HTTP 方法（GET/POST/PUT/DELETE）是否正确</li>
 *   <li>Controller Bean 验证：验证三个 Controller 都已注册且能获取</li>
 *   <li>Controller 依赖注入验证：验证 Controller 中注入的 Service 可正常调用</li>
 * </ol>
 */
@Slf4j
public class WebMvcExampleTest {

    /**
     * 测试：路由路径验证 — RouteRegistry 中包含预期的路由路径
     */
    @Test
    void testRoutePathRegistration() throws Exception {
        log.info("\n========== 测试：路由路径注册验证 ==========\n");

        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, "--server.port=0").run()) {
            RouteRegistry routeRegistry = context.getBean("routeRegistry", RouteRegistry.class);
            assertNotNull(routeRegistry, "routeRegistry 不应为空");

            List<RouteDefinition> routes = routeRegistry.getAllRoutes();
            assertFalse(routes.isEmpty(), "应至少注册一个路由");

            List<String> paths = routes.stream().map(RouteDefinition::path).toList();

            // 验证 UserController 路由路径
            assertTrue(paths.contains("/api/users"), "应包含路由路径 /api/users");
            assertTrue(paths.contains("/api/users/{id}"), "应包含路由路径 /api/users/{id}");

            // 验证 ProductController 路由路径
            assertTrue(paths.contains("/api/products"), "应包含路由路径 /api/products");
            assertTrue(paths.contains("/api/products/{id}"), "应包含路由路径 /api/products/{id}");

            // 验证 OrderController 路由路径
            assertTrue(paths.contains("/api/orders"), "应包含路由路径 /api/orders");
            assertTrue(paths.contains("/api/orders/{id}"), "应包含路由路径 /api/orders/{id}");

            log.info("✅ 路由路径验证成功，已注册 {} 个路由", routes.size());
        }
    }

    /**
     * 测试：HTTP 方法验证 — 验证路由注册的 HTTP 方法正确（GET/POST/PUT/DELETE）
     */
    @Test
    void testRouteHttpMethodRegistration() throws Exception {
        log.info("\n========== 测试：路由 HTTP 方法验证 ==========\n");

        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, "--server.port=0").run()) {
            RouteRegistry routeRegistry = context.getBean("routeRegistry", RouteRegistry.class);
            List<RouteDefinition> routes = routeRegistry.getAllRoutes();

            // GET /api/users
            assertTrue(hasRoute(routes, "/api/users", HttpMethod.GET), "应有 GET /api/users");
            // POST /api/users
            assertTrue(hasRoute(routes, "/api/users", HttpMethod.POST), "应有 POST /api/users");
            // PUT /api/users/{id}
            assertTrue(hasRoute(routes, "/api/users/{id}", HttpMethod.PUT), "应有 PUT /api/users/{id}");
            // DELETE /api/users/{id}
            assertTrue(hasRoute(routes, "/api/users/{id}", HttpMethod.DELETE), "应有 DELETE /api/users/{id}");

            // GET /api/products
            assertTrue(hasRoute(routes, "/api/products", HttpMethod.GET), "应有 GET /api/products");
            // POST /api/products
            assertTrue(hasRoute(routes, "/api/products", HttpMethod.POST), "应有 POST /api/products");

            // GET /api/orders
            assertTrue(hasRoute(routes, "/api/orders", HttpMethod.GET), "应有 GET /api/orders");
            // POST /api/orders
            assertTrue(hasRoute(routes, "/api/orders", HttpMethod.POST), "应有 POST /api/orders");

            log.info("✅ HTTP 方法验证成功");
        }
    }

    /**
     * 测试：Controller Bean 验证 — 三个 Controller 都已注册且能获取
     */
    @Test
    void testControllerBeanRegistration() throws Exception {
        log.info("\n========== 测试：Controller Bean 注册验证 ==========\n");

        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, "--server.port=0").run()) {
            assertTrue(context.containsBean("userController"), "userController 应已注册");
            assertTrue(context.containsBean("productController"), "productController 应已注册");
            assertTrue(context.containsBean("orderController"), "orderController 应已注册");

            UserController userController = context.getBean("userController", UserController.class);
            assertNotNull(userController, "userController 不应为空");

            ProductController productController = context.getBean("productController", ProductController.class);
            assertNotNull(productController, "productController 不应为空");

            OrderController orderController = context.getBean("orderController", OrderController.class);
            assertNotNull(orderController, "orderController 不应为空");

            log.info("✅ Controller Bean 注册验证成功");
        }
    }

    /**
     * 测试：Controller 依赖注入验证 — Controller 中 @Inject 的 Service 已被正确注入
     */
    @Test
    void testControllerDependencyInjection() throws Exception {
        log.info("\n========== 测试：Controller 依赖注入验证 ==========\n");

        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, "--server.port=0").run()) {
            UserController userController = context.getBean("userController", UserController.class);
            assertNotNull(userController, "userController 不应为空");
            // 调用 Controller 方法验证 Service 已注入（获取所有用户，结果不为空）
            assertNotNull(userController.getAllUsers(), "userController.getAllUsers() 不应返回 null");

            ProductController productController = context.getBean("productController", ProductController.class);
            assertNotNull(productController, "productController 不应为空");
            assertNotNull(productController.getAllProducts(), "productController.getAllProducts() 不应返回 null");

            OrderController orderController = context.getBean("orderController", OrderController.class);
            assertNotNull(orderController, "orderController 不应为空");
            assertNotNull(orderController.getAllOrders(), "orderController.getAllOrders() 不应返回 null");

            log.info("✅ Controller 依赖注入验证成功");
        }
    }

    private boolean hasRoute(List<RouteDefinition> routes, String path, HttpMethod method) {
        return routes.stream().anyMatch(r -> r.path().equals(path) && r.httpMethod() == method);
    }
}
