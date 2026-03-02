package org.moper.cap.example;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.moper.cap.boot.application.impl.DefaultCapApplication;
import org.moper.cap.core.context.RuntimeContext;
import org.moper.cap.example.aop.AspectLogger;
import org.moper.cap.example.bean.ConfigurableService;
import org.moper.cap.example.bean.DirectService;
import org.moper.cap.example.bean.DynamicConfigService;
import org.moper.cap.example.bean.GreetingServiceImpl;
import org.moper.cap.example.bean.IGreetingService;
import org.moper.cap.example.bean.PaymentService;
import org.moper.cap.example.bean.PrototypeService;
import org.moper.cap.example.bean.SingletonService;
import org.moper.cap.example.model.AppConfig;
import org.moper.cap.example.model.DatabaseConnection;
import org.moper.cap.example.service.OrderService;
import org.moper.cap.example.service.ProductService;
import org.moper.cap.example.service.UserService;
import org.moper.cap.property.event.PropertyRemoveOperation;
import org.moper.cap.property.event.PropertySetOperation;
import org.moper.cap.property.officer.PropertyOfficer;
import org.moper.cap.property.publisher.PropertyPublisher;

import static org.junit.jupiter.api.Assertions.*;

/**
 * cap-example 集成测试，全面验证框架所有 BootstrapRunner 的功能。
 *
 * <p>测试覆盖范围：
 * <ol>
 *   <li>Bean 扫描与注册（ClassBeanRegisterBootstrapRunner）</li>
 *   <li>{@link org.moper.cap.bean.annotation.Inject} 构造函数注入（BeanInjectionBootstrapRunner）</li>
 *   <li>{@link org.moper.cap.bean.annotation.Inject} 字段注入（BeanInjectionBootstrapRunner）</li>
 *   <li>显式 Bean 名称注入（BeanInjectionBootstrapRunner）</li>
 *   <li>多名称与别名功能（ClassBeanRegisterBootstrapRunner）</li>
 *   <li>初始化方法的调用（LifecycleMethodRegisterBootstrapRunner）</li>
 *   <li>工厂 Bean 覆盖普通 Bean（FactoryBeanRegisterBootstrapRunner）</li>
 *   <li>单例作用域（PreInstantiateSingletonBootstrapRunner）</li>
 *   <li>原型作用域（PreInstantiateSingletonBootstrapRunner）</li>
 *   <li>Bean 的完整生命周期（LifecycleMethodRegisterBootstrapRunner）</li>
 *   <li>{@link org.moper.cap.property.annotation.Value} 属性注入（PropertyValueBootstrapRunner）</li>
 *   <li>{@link org.moper.cap.property.annotation.Subscription} 属性监听（PropertySubscriptionBootstrapRunner）</li>
 *   <li>AOP JDK 动态代理（AopBootstrapRunner）</li>
 *   <li>AOP CGLib 动态代理（AopBootstrapRunner）</li>
 *   <li>命令行参数属性注入（CommandArgumentsBootstrapRunner）</li>
 *   <li>系统环境变量属性（SystemPropertyBootstrapRunner）</li>
 *   <li>Profile 配置文件加载（ActiveProfilePropertyBootstrapRunner）</li>
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

    /**
     * 测试系统属性注入（SystemPropertyBootstrapRunner）。
     *
     * <p>验证 OS 环境变量和 JVM 系统属性已被注册到 PropertyOfficer 中。
     */
    @Test
    void testSystemProperties() throws Exception {
        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class).run()) {
            PropertyOfficer officer = context.getPropertyOfficer();
            // Java 系统属性 java.version 必然存在
            Object javaVersion = officer.getRawPropertyValue("java.version");
            assertNotNull(javaVersion, "系统属性 java.version 应已注册到 PropertyOfficer");
        }
    }

    /**
     * 测试命令行参数注入（CommandArgumentsBootstrapRunner）。
     *
     * <p>传入 {@code --app.env=test} 命令行参数，验证属性可从 PropertyOfficer 中读取。
     */
    @Test
    void testCommandLineArguments() throws Exception {
        try (RuntimeContext context = new DefaultCapApplication(
                ExampleApplication.class, "--app.env=test").run()) {
            PropertyOfficer officer = context.getPropertyOfficer();
            Object envValue = officer.getRawPropertyValue("app.env");
            assertNotNull(envValue, "命令行参数 app.env 应已注册");
            assertEquals("test", envValue.toString(), "命令行参数值应为 test");
        }
    }

    /**
     * 测试 Profile 配置文件加载（ActiveProfilePropertyBootstrapRunner）。
     *
     * <p>通过命令行参数激活 {@code dev} profile，验证 {@code application-dev.yaml}
     * 中的属性被正确加载，dev 环境的属性值覆盖了基础配置。
     */
    @Test
    void testActiveProfileLoading() throws Exception {
        try (RuntimeContext context = new DefaultCapApplication(
                ExampleApplication.class,
                "--application.profiles.active=dev").run()) {
            PropertyOfficer officer = context.getPropertyOfficer();
            // application-dev.yaml 中 app.debug=true，基础配置中无此属性
            Object debug = officer.getRawPropertyValue("app.debug");
            assertNotNull(debug, "dev profile 的 app.debug 属性应已加载");
            assertEquals("true", debug.toString(), "dev profile 中 app.debug 应为 true");
        }
    }

    /**
     * 测试 {@link org.moper.cap.property.annotation.Value} 属性注入（PropertyValueBootstrapRunner）。
     *
     * <p>验证 {@link ConfigurableService} 中各种 {@code @Value} 表达式均被正确解析和注入：
     * <ul>
     *   <li>{@code ${app.name}} — 直接从配置文件注入</li>
     *   <li>{@code ${app.port:8080}} — 带默认值，属性存在时使用属性值</li>
     *   <li>{@code ${db.url}} — 注入 DB 连接 URL</li>
     *   <li>{@code ${app.version:1.0.0}} — 属性存在时使用属性值</li>
     *   <li>{@code ${missing.key:defaultFallback}} — 属性不存在时使用默认值</li>
     * </ul>
     */
    @Test
    void testValueAnnotationInjection() throws Exception {
        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class).run()) {
            ConfigurableService svc = context.getBean("configurableService", ConfigurableService.class);
            assertNotNull(svc, "configurableService 不应为 null");
            assertEquals("cap-example", svc.getAppName(), "@Value(${app.name}) 应注入 cap-example");
            assertEquals(8080, svc.getAppPort(), "@Value(${app.port:8080}) 应注入 8080");
            assertEquals("jdbc:example://localhost:5432/example", svc.getDbUrl(),
                    "@Value(${db.url}) 应注入数据库 URL");
            assertEquals("1.0-SNAPSHOT", svc.getAppVersion(),
                    "@Value(${app.version:1.0.0}) 属性存在时应使用属性值");
            assertEquals("defaultFallback", svc.getMissingKeyWithDefault(),
                    "@Value(${missing.key:defaultFallback}) 属性不存在时应使用默认值");
        }
    }

    /**
     * 测试属性订阅监听（PropertySubscriptionBootstrapRunner）。
     *
     * <p>验证 {@link DynamicConfigService} 中 {@link org.moper.cap.property.annotation.Subscriber}
     * 字段在属性变更时的回调行为：
     * <ul>
     *   <li>{@code onSet} 回调将最新值赋给字段</li>
     *   <li>{@code onRemoved} 回调在属性被移除时被调用</li>
     * </ul>
     */
    @Test
    void testPropertySubscription() throws Exception {
        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class).run()) {
            PropertyOfficer officer = context.getPropertyOfficer();
            PropertyPublisher publisher = officer.getPublisher("example-publisher");

            DynamicConfigService svc = context.getBean("dynamicConfigService", DynamicConfigService.class);

            // 触发 dynamic.debug 属性变更
            publisher.publish(new PropertySetOperation("dynamic.debug", "true"));
            assertEquals(Boolean.TRUE, svc.getDebugEnabled(),
                    "dynamic.debug 设置为 true 后，debugEnabled 应更新");

            // 触发 dynamic.cache.ttl 属性变更
            publisher.publish(new PropertySetOperation("dynamic.cache.ttl", "300"));
            assertEquals(300, svc.getCacheTtl(), "dynamic.cache.ttl 设置为 300 后，cacheTtl 应更新");

            // 触发 dynamic.debug 属性移除
            publisher.publish(new PropertyRemoveOperation("dynamic.debug"));
            assertTrue(svc.isDebugRemovedCalled(), "dynamic.debug 被移除后，onDebugRemoved 应被调用");
        }
    }

    /**
     * 测试通过 {@link org.moper.cap.bean.annotation.Inject} 显式指定 Bean 名称注入（BeanInjectionBootstrapRunner）。
     *
     * <p>{@link PaymentService} 使用 {@code @Inject("product")} 注入名称为
     * {@code "product"} 的 {@link org.moper.cap.example.service.ProductService} Bean，
     * 验证显式名称注入正常工作。
     */
    @Test
    void testExplicitBeanNameInjection() throws Exception {
        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class).run()) {
            PaymentService paymentService = context.getBean("paymentService", PaymentService.class);
            assertNotNull(paymentService, "paymentService 不应为 null");
            String result = paymentService.pay(1L, 100L, 99.9);
            assertTrue(result.contains("User#1"), "支付结果应包含用户信息");
            assertTrue(result.contains("Product#100"), "支付结果应包含商品信息");
        }
    }

    /**
     * 测试显式 {@link org.moper.cap.bean.definition.BeanScope#SINGLETON} 作用域（PreInstantiateSingletonBootstrapRunner）。
     *
     * <p>验证多次获取 {@link SingletonService} 始终返回同一实例。
     */
    @Test
    void testExplicitSingletonScope() throws Exception {
        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class).run()) {
            SingletonService s1 = context.getBean("singletonService", SingletonService.class);
            SingletonService s2 = context.getBean("singletonService", SingletonService.class);
            assertNotNull(s1, "singletonService 不应为 null");
            assertSame(s1, s2, "SINGLETON Bean 多次获取应为同一实例");
            assertTrue(context.isSingleton("singletonService"), "singletonService 应为 SINGLETON 作用域");
        }
    }

    /**
     * 测试显式 {@link org.moper.cap.bean.definition.BeanScope#PROTOTYPE} 作用域（PreInstantiateSingletonBootstrapRunner）。
     *
     * <p>验证每次获取 {@link PrototypeService} 都会返回新实例。
     */
    @Test
    void testExplicitPrototypeScope() throws Exception {
        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class).run()) {
            PrototypeService p1 = context.getBean("prototypeService", PrototypeService.class);
            PrototypeService p2 = context.getBean("prototypeService", PrototypeService.class);
            assertNotNull(p1, "prototypeService 不应为 null");
            assertNotSame(p1, p2, "PROTOTYPE Bean 每次 getBean 应返回新实例");
            assertTrue(context.isPrototype("prototypeService"), "prototypeService 应为 PROTOTYPE 作用域");
        }
    }

    /**
     * 测试 AOP JDK 动态代理（AopBootstrapRunner）。
     *
     * <p>{@link GreetingServiceImpl} 实现了 {@link IGreetingService} 接口，
     * AOP 框架使用 JDK Proxy 代理。验证切面 {@link AspectLogger} 中的
     * {@link org.moper.cap.aop.annotation.Before} 和 {@link org.moper.cap.aop.annotation.After}
     * 通知在方法执行前后被触发。
     */
    @Test
    void testJdkProxyAop() throws Exception {
        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class).run()) {
            IGreetingService greetingService = context.getBean("greetingServiceImpl", IGreetingService.class);
            assertNotNull(greetingService, "greetingServiceImpl 不应为 null");
            // 验证是 JDK 代理（不是 GreetingServiceImpl 实例）
            assertFalse(greetingService instanceof GreetingServiceImpl,
                    "JDK Proxy 不应是 GreetingServiceImpl 的实例");

            AspectLogger aspectLogger = context.getBean("aspectLogger", AspectLogger.class);
            int beforeBefore = aspectLogger.getBeforeCount();
            int afterBefore = aspectLogger.getAfterCount();

            String result = greetingService.greet("World");
            assertEquals("Hello, World!", result, "greet 返回值应正确");

            assertEquals(beforeBefore + 1, aspectLogger.getBeforeCount(),
                    "@Before 通知应在 greet 执行前触发一次");
            assertEquals(afterBefore + 1, aspectLogger.getAfterCount(),
                    "@After 通知应在 greet 执行后触发一次");
        }
    }

    /**
     * 测试 AOP CGLib 动态代理（AopBootstrapRunner）。
     *
     * <p>{@link DirectService} 未实现任何接口，AOP 框架使用 CGLib 子类代理。
     * 验证切面 {@link AspectLogger} 中的 {@link org.moper.cap.aop.annotation.Before}
     * 和 {@link org.moper.cap.aop.annotation.After} 通知在方法执行前后被触发。
     */
    @Test
    void testCglibProxyAop() throws Exception {
        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class).run()) {
            DirectService directService = context.getBean("directService", DirectService.class);
            assertNotNull(directService, "directService 不应为 null");
            // 验证是 CGLib 代理（是 DirectService 的子类实例，但不是 DirectService 本身）
            assertTrue(directService instanceof DirectService,
                    "CGLib Proxy 应是 DirectService 的子类实例");
            assertNotEquals(DirectService.class, directService.getClass(),
                    "CGLib Proxy 不应是 DirectService 类本身（应为生成的子类）");

            AspectLogger aspectLogger = context.getBean("aspectLogger", AspectLogger.class);
            int beforeBefore = aspectLogger.getBeforeCount();
            int afterBefore = aspectLogger.getAfterCount();

            String result = directService.execute("test-command");
            assertEquals("Executed: test-command", result, "execute 返回值应正确");

            assertEquals(beforeBefore + 1, aspectLogger.getBeforeCount(),
                    "@Before 通知应在 execute 执行前触发一次");
            assertEquals(afterBefore + 1, aspectLogger.getAfterCount(),
                    "@After 通知应在 execute 执行后触发一次");
        }
    }
}
