package org.moper.cap.example;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.moper.cap.boot.application.impl.DefaultCapApplication;
import org.moper.cap.core.context.RuntimeContext;
import org.moper.cap.example.model.Order;
import org.moper.cap.example.service.OrderService;
import org.moper.cap.example.service.ProductService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 事务模块集成测试
 *
 * <p>演示内容：
 * <ol>
 *   <li>基本事务 ({@code @Transactional} + {@code REQUIRED})</li>
 *   <li>嵌套事务 ({@code @Transactional} + {@code NESTED})</li>
 *   <li>独立事务 ({@code @Transactional} + {@code REQUIRES_NEW})</li>
 *   <li>事务回滚</li>
 * </ol>
 */
@Slf4j
public class TransactionExampleTest {

    /**
     * 测试：基本事务 - 成功创建订单
     */
    @Test
    void testBasicTransaction_CreateOrderSuccess() throws Exception {
        log.info("\n========== 测试：基本事务 - 成功创建订单 ==========\n");

        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, "--server.port=0").run()) {
            OrderService orderService = context.getBean("orderService", OrderService.class);

            Order order = orderService.createOrder(1, 1, 5);

            assertNotNull(order);
            assertEquals(1, order.getUserId());
            assertEquals(1, order.getProductId());
            assertEquals(5, order.getQuantity());

            log.info("✅ 订单创建成功: {}", order.getId());
        }
    }

    /**
     * 测试：基本事务 - 用户不存在，事务回滚
     */
    @Test
    void testBasicTransaction_RollbackOnUserNotFound() throws Exception {
        log.info("\n========== 测试：基本事务 - 用户不存在，事务回滚 ==========\n");

        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, "--server.port=0").run()) {
            OrderService orderService = context.getBean("orderService", OrderService.class);
            ProductService productService = context.getBean("productService", ProductService.class);

            long initialStock = productService.getProductById(1).getStock();

            assertThrows(IllegalArgumentException.class, () -> {
                orderService.createOrder(999, 1, 5);  // 用户不存在
            });

            // 验证库存未被扣减（事务回滚）
            long currentStock = productService.getProductById(1).getStock();
            assertEquals(initialStock, currentStock);

            log.info("✅ 事务成功回滚，库存未变化");
        }
    }

    /**
     * 测试：基本事务 - 库存不足，事务回滚
     */
    @Test
    void testBasicTransaction_RollbackOnInsufficientStock() throws Exception {
        log.info("\n========== 测试：基本事务 - 库存不足，事务回滚 ==========\n");

        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, "--server.port=0").run()) {
            OrderService orderService = context.getBean("orderService", OrderService.class);

            assertThrows(IllegalArgumentException.class, () -> {
                orderService.createOrder(1, 1, 10000);  // 库存不足
            });

            log.info("✅ 事务成功回滚，订单未创建");
        }
    }

    /**
     * 测试：嵌套事务
     */
    @Test
    void testNestedTransaction() throws Exception {
        log.info("\n========== 测试：嵌套事务 ==========\n");

        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, "--server.port=0").run()) {
            OrderService orderService = context.getBean("orderService", OrderService.class);

            // 先创建一个订单
            Order order = orderService.createOrder(1, 1, 5);
            log.info("创建订单: {}", order.getId());

            // 转移订单（演示嵌套事务）
            Order transferred = orderService.transferOrder(order.getId(), 1, 2);
            assertEquals(2, transferred.getUserId());

            log.info("✅ 嵌套事务成功，订单转移成功");
        }
    }

    /**
     * 测试：批量创建订单（REQUIRES_NEW）
     */
    @Test
    void testBatchCreateOrders() throws Exception {
        log.info("\n========== 测试：批量创建订单 (REQUIRES_NEW) ==========\n");

        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, "--server.port=0").run()) {
            OrderService orderService = context.getBean("orderService", OrderService.class);

            List<OrderService.OrderRequest> requests = new ArrayList<>();
            requests.add(new OrderService.OrderRequest(1, 1, 3));
            requests.add(new OrderService.OrderRequest(1, 2, 2));
            requests.add(new OrderService.OrderRequest(2, 1, 4));

            List<Order> orders = orderService.batchCreateOrders(requests);

            assertEquals(3, orders.size());
            log.info("✅ 批量订单创建成功: {} 个订单", orders.size());
        }
    }

    /**
     * 测试：事务相关 Bean 的注册
     */
    @Test
    void testTransactionBeanRegistration() throws Exception {
        log.info("\n========== 测试：事务相关 Bean 的注册 ==========\n");

        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, "--server.port=0").run()) {
            OrderService orderService = context.getBean("orderService", OrderService.class);
            assertNotNull(orderService);

            try {
                java.lang.reflect.Method method = OrderService.class.getDeclaredMethod("createOrder", long.class, long.class, int.class);
                assertTrue(method.isAnnotationPresent(org.moper.cap.transaction.annotation.Transactional.class));
                log.info("✅ OrderService.createOrder 已标注 @Transactional");
            } catch (NoSuchMethodException e) {
                fail("Method not found");
            }
        }
    }
}
