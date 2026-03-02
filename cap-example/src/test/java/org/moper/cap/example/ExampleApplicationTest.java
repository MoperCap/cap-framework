package org.moper.cap.example;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.moper.cap.boot.application.impl.DefaultCapApplication;
import org.moper.cap.core.context.RuntimeContext;
import org.moper.cap.example.model.AppConfig;
import org.moper.cap.example.model.DatabaseConnection;
import org.moper.cap.example.service.OrderService;
import org.moper.cap.example.service.ProductService;
import org.moper.cap.example.service.UserService;

import static org.junit.jupiter.api.Assertions.*;

/**
 * cap-example 集成测试，全面验证 IOC 容器的各项能力。
 *
 * <p>测试覆盖范围：
 * <ol>
 *   <li>Bean 扫描与注册</li>
 *   <li>{@link org.moper.cap.bean.annotation.Inject} 构造函数注入</li>
 *   <li>{@link org.moper.cap.bean.annotation.Inject} 字段注入</li>
 *   <li>多名称与别名功能</li>
 *   <li>初始化方法的调用</li>
 *   <li>工厂 Bean 覆盖普通 Bean</li>
 *   <li>单例作用域</li>
 *   <li>原型作用域</li>
 *   <li>Bean 的完整生命周期（销毁方法）</li>
 * </ol>
 */
@Slf4j
public class ExampleApplicationTest {

    /**
     * 测试 Bean 的扫描和注册。
     *
     * <p>验证所有预期 Bean 均已在容器中正确注册，包括主名称和别名。
     */
    @Test
    void testBeanScanningAndRegistration() throws Exception {
        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class).run()) {
            assertTrue(context.containsBean("userService"), "userService 应已注册");
            assertTrue(context.containsBean("product"), "product 应已注册（主名称）");
            assertTrue(context.containsBean("productService"), "productService 应已注册（别名）");
            assertTrue(context.containsBean("orderService"), "orderService 应已注册");
            assertTrue(context.containsBean("databaseConnection"), "databaseConnection 应已注册");
            assertTrue(context.containsBean("appConfig"), "appConfig 应已注册（工厂方法）");
        }
    }

    /**
     * 测试 {@link org.moper.cap.bean.annotation.Inject} 构造函数注入。
     *
     * <p>{@link UserService} 通过 {@code @Inject} 构造函数接收 {@link DatabaseConnection}，
     * 验证注入成功后业务方法可正常调用。
     */
    @Test
    void testConstructorInjection() throws Exception {
        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class).run()) {
            UserService userService = context.getBean("userService", UserService.class);
            assertNotNull(userService, "userService 不应为 null");
            String result = userService.findUser(1L);
            assertNotNull(result);
            assertTrue(result.contains("User#1"), "findUser 结果应包含 User#1");
            // URL 来自工厂方法创建的 DatabaseConnection
            assertTrue(result.contains("jdbc:example://localhost:5432/example"),
                    "findUser 结果应包含工厂方法指定的数据库 URL");
        }
    }

    /**
     * 测试 {@link org.moper.cap.bean.annotation.Inject} 字段注入。
     *
     * <p>{@link OrderService} 通过 {@code @Inject} 字段注入 {@link UserService}
     * 和 {@link ProductService}，验证注入成功后业务方法可正常调用。
     */
    @Test
    void testFieldInjection() throws Exception {
        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class).run()) {
            OrderService orderService = context.getBean("orderService", OrderService.class);
            assertNotNull(orderService, "orderService 不应为 null");
            String order = orderService.createOrder(1L, 100L);
            assertNotNull(order);
            assertTrue(order.contains("User#1"), "订单应包含用户信息");
            assertTrue(order.contains("Product#100"), "订单应包含商品信息");
        }
    }

    /**
     * 测试多名称和别名功能。
     *
     * <p>{@link ProductService} 注册了主名称 {@code "product"} 和别名 {@code "productService"}，
     * 两者均可从容器中获取 Bean 定义。
     */
    @Test
    void testMultipleNamesAndAlias() throws Exception {
        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class).run()) {
            // 主名称查找
            ProductService byPrimary = context.getBean("product", ProductService.class);
            assertNotNull(byPrimary, "按主名称 'product' 查找应成功");

            // 别名查找
            ProductService byAlias = context.getBean("productService", ProductService.class);
            assertNotNull(byAlias, "按别名 'productService' 查找应成功");

            // 验证别名指向同一 Bean 定义（prototype 下每次 getBean 创建新实例，但类型相同）
            assertEquals(ProductService.class, byPrimary.getClass());
            assertEquals(ProductService.class, byAlias.getClass());
        }
    }

    /**
     * 测试初始化方法的调用。
     *
     * <p>验证 {@link UserService#init()} 和 {@link DatabaseConnection#connect()}
     * 在 Bean 就绪后已被自动调用。
     */
    @Test
    void testInitMethodInvocation() throws Exception {
        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class).run()) {
            UserService userService = context.getBean("userService", UserService.class);
            assertTrue(userService.isInitialized(), "UserService.init() 应已被调用");

            DatabaseConnection db = context.getBean("databaseConnection", DatabaseConnection.class);
            assertTrue(db.isConnected(), "DatabaseConnection.connect() 应已被调用");
        }
    }

    /**
     * 测试工厂 Bean 覆盖普通 Bean。
     *
     * <p>{@link org.moper.cap.example.factory.AppBeanFactory#databaseConnection()} 静态工厂方法
     * 的 Bean 名称为 {@code "databaseConnection"}，与 {@link DatabaseConnection} 类级别的
     * Bean 同名，工厂方法定义优先，覆盖类级别定义。验证获取到的是工厂方法创建的实例。
     */
    @Test
    void testFactoryBeanOverridesClassBean() throws Exception {
        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class).run()) {
            DatabaseConnection db = context.getBean("databaseConnection", DatabaseConnection.class);
            assertEquals("jdbc:example://localhost:5432/example", db.getUrl(),
                    "databaseConnection 应由工厂方法创建，URL 应为工厂方法指定的值");
        }
    }

    /**
     * 测试单例作用域。
     *
     * <p>多次获取同一 Bean 应返回同一实例。
     */
    @Test
    void testSingletonScope() throws Exception {
        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class).run()) {
            UserService us1 = context.getBean("userService", UserService.class);
            UserService us2 = context.getBean("userService", UserService.class);
            assertSame(us1, us2, "单例 Bean 多次获取应为同一实例");
            assertTrue(context.isSingleton("userService"), "userService 应为 SINGLETON 作用域");
        }
    }

    /**
     * 测试原型作用域。
     *
     * <p>{@link ProductService} 的作用域为 {@link org.moper.cap.bean.definition.BeanScope#PROTOTYPE}，
     * 每次 {@code getBean} 应返回不同的新实例。
     */
    @Test
    void testPrototypeScope() throws Exception {
        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class).run()) {
            ProductService p1 = context.getBean("product", ProductService.class);
            ProductService p2 = context.getBean("product", ProductService.class);
            assertNotSame(p1, p2, "原型 Bean 每次 getBean 应返回新实例");
            assertTrue(context.isPrototype("product"), "product 应为 PROTOTYPE 作用域");
        }
    }

    /**
     * 测试 Bean 的完整生命周期（销毁方法）。
     *
     * <p>容器关闭后，单例 Bean 的销毁方法应被调用。
     */
    @Test
    void testDestroyMethodInvocation() throws Exception {
        UserService userService;
        DatabaseConnection db;

        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class).run()) {
            userService = context.getBean("userService", UserService.class);
            db = context.getBean("databaseConnection", DatabaseConnection.class);

            // 容器运行期间，Bean 应处于初始化状态
            assertTrue(userService.isInitialized(), "容器运行期间 UserService 应处于初始化状态");
            assertTrue(db.isConnected(), "容器运行期间 DatabaseConnection 应处于连接状态");
        }
        // 容器关闭后，销毁方法应已被调用
        assertFalse(userService.isInitialized(), "容器关闭后 UserService.destroy() 应已被调用");
        assertFalse(db.isConnected(), "容器关闭后 DatabaseConnection.disconnect() 应已被调用");
    }

    /**
     * 测试非静态工厂方法创建的 Bean。
     *
     * <p>{@link org.moper.cap.example.factory.AppBeanFactory#appConfig()} 是非静态工厂方法，
     * 验证 {@link AppConfig} Bean 被正确创建。
     */
    @Test
    void testInstanceFactoryMethod() throws Exception {
        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class).run()) {
            AppConfig appConfig = context.getBean("appConfig", AppConfig.class);
            assertNotNull(appConfig, "appConfig 不应为 null");
            assertEquals("cap-example", appConfig.getAppName());
            assertEquals("1.0-SNAPSHOT", appConfig.getVersion());
        }
    }
}
