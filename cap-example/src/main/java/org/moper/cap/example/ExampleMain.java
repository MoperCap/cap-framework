package org.moper.cap.example;

import org.moper.cap.boot.application.impl.DefaultCapApplication;
import org.moper.cap.core.context.RuntimeContext;
import org.moper.cap.example.model.AppConfig;
import org.moper.cap.example.model.DatabaseConnection;
import org.moper.cap.example.service.OrderService;
import org.moper.cap.example.service.ProductService;
import org.moper.cap.example.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 示例应用启动入口，展示 cap-framework IOC 功能的完整使用方式。
 *
 * <p>演示内容：
 * <ol>
 *   <li>初始化框架并获取 {@link RuntimeContext}</li>
 *   <li>从容器中获取 Bean 并调用业务方法</li>
 *   <li>Bean 初始化方法的自动调用</li>
 *   <li>工厂方法创建的 Bean（{@link DatabaseConnection} 覆盖场景）</li>
 *   <li>容器关闭时 Bean 销毁方法的自动调用</li>
 * </ol>
 */
public class ExampleMain {

    private static final Logger log = LoggerFactory.getLogger(ExampleMain.class);

    public static void main(String[] args) throws Exception {
        log.info("=== cap-framework IOC 示例应用启动 ===");

        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, args).run()) {

            // 获取应用配置（由工厂方法创建）
            AppConfig appConfig = context.getBean("appConfig", AppConfig.class);
            log.info("应用配置: {}", appConfig);

            // 获取数据库连接（由工厂方法创建，覆盖了类级别的 Bean）
            DatabaseConnection db = context.getBean("databaseConnection", DatabaseConnection.class);
            log.info("数据库连接: url={}, connected={}", db.getUrl(), db.isConnected());

            // 获取用户服务（构造函数注入 DatabaseConnection，init 方法已被调用）
            UserService userService = context.getBean("userService", UserService.class);
            log.info("UserService initialized={}", userService.isInitialized());
            log.info(userService.findUser(1L));
            log.info(userService.findUser(2L));

            // 获取商品服务（多名称 Bean，prototype 作用域）
            ProductService productByPrimary = context.getBean("product", ProductService.class);
            ProductService productByAlias = context.getBean("productService", ProductService.class);
            log.info("product Bean (主名称): {}", productByPrimary.findProduct(100L));
            log.info("productService Bean (别名): {}", productByAlias.findProduct(200L));
            log.info("是否为同一实例（prototype 应为 false）: {}", productByPrimary == productByAlias);

            // 获取订单服务（字段注入 UserService 和 ProductService）
            OrderService orderService = context.getBean("orderService", OrderService.class);
            log.info(orderService.createOrder(1L, 100L));
            log.info(orderService.createOrder(2L, 200L));

            log.info("=== 容器关闭中，销毁方法将被调用 ===");
        }

        log.info("=== 应用已退出，Bean 销毁完成 ===");
    }
}
