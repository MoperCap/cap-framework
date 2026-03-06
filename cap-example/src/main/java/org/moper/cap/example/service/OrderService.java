package org.moper.cap.example.service;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.bean.annotation.Inject;
import org.moper.cap.example.model.Order;
import org.moper.cap.transaction.annotation.IsolationLevel;
import org.moper.cap.transaction.annotation.Propagation;
import org.moper.cap.transaction.annotation.Transactional;
import org.moper.cap.transaction.template.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 订单服务 - 演示事务功能
 *
 * <p>支持以下事务场景：
 * <ol>
 *   <li>{@link #createOrder} - 创建订单（演示基本事务）</li>
 *   <li>{@link #transferOrder} - 订单转移（演示嵌套事务）</li>
 *   <li>{@link #batchCreateOrders} - 批量创建订单（演示独立事务）</li>
 * </ol>
 */
@Slf4j
@Capper
public class OrderService {

    @Inject
    UserService userService;

    @Inject
    ProductService productService;

    @Inject
    private TransactionTemplate txTemplate;

    private final Map<Long, Order> orderStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    /**
     * 创建订单 - 基本事务示例
     *
     * <p>{@code @Transactional} 注解确保整个方法的原子性：
     * <ol>
     *   <li>验证用户存在</li>
     *   <li>验证商品存在</li>
     *   <li>扣减商品库存</li>
     *   <li>创建订单记录</li>
     * </ol>
     * 如果任何一步失败，整个事务回滚。
     */
    @Transactional(
        propagation = Propagation.REQUIRED,
        isolation = IsolationLevel.READ_COMMITTED,
        timeout = 30
    )
    public Order createOrder(long userId, long productId, int quantity) {
        log.info("=== 开始创建订单 [事务] ===");
        log.info("创建订单: userId={}, productId={}, quantity={}", userId, productId, quantity);

        // 1. 验证用户
        if (!userService.userExists(userId)) {
            log.error("用户不存在: userId={}", userId);
            throw new IllegalArgumentException("用户不存在");
        }

        // 2. 验证商品
        if (!productService.productExists(productId)) {
            log.error("商品不存在: productId={}", productId);
            throw new IllegalArgumentException("商品不存在");
        }

        // 3. 扣减库存
        if (!productService.decrementStock(productId, quantity)) {
            log.error("库存不足: productId={}, required={}", productId, quantity);
            throw new IllegalArgumentException("库存不足");
        }

        // 4. 创建订单
        Order order = new Order();
        order.setId(idGenerator.incrementAndGet());
        order.setUserId(userId);
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setCreateTime(LocalDateTime.now());

        orderStore.put(order.getId(), order);
        log.info("订单创建成功: orderId={}", order.getId());
        log.info("=== 订单创建事务提交 ===");

        return order;
    }

    /**
     * 订单转移 - 嵌套事务示例
     *
     * <p>演示 {@code Propagation.NESTED} 的用法：
     * <ol>
     *   <li>验证订单归属</li>
     *   <li>将订单转移给目标用户（嵌套事务，支持独立回滚）</li>
     * </ol>
     * 如果嵌套步骤失败，不会影响外层事务。
     */
    @Transactional(
        propagation = Propagation.REQUIRED,
        isolation = IsolationLevel.REPEATABLE_READ
    )
    public Order transferOrder(long orderId, long fromUserId, long toUserId) {
        log.info("=== 开始转移订单 [外层事务] ===");

        Order order = orderStore.get(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }

        if (order.getUserId() != fromUserId) {
            throw new IllegalArgumentException("订单不属于该用户");
        }

        // 调用嵌套事务方法
        try {
            transferToUser(order, toUserId);
        } catch (Exception e) {
            log.error("转移失败，但外层事务继续: {}", e.getMessage());
        }

        log.info("=== 订单转移事务提交 ===");
        return order;
    }

    /**
     * 内部嵌套事务方法 - 将订单转移给目标用户。
     */
    @Transactional(
        propagation = Propagation.NESTED,
        isolation = IsolationLevel.REPEATABLE_READ
    )
    private void transferToUser(Order order, long toUserId) {
        log.info("=== 嵌套事务：转移订单到用户 ===");

        if (!userService.userExists(toUserId)) {
            throw new IllegalArgumentException("目标用户不存在");
        }

        order.setUserId(toUserId);
        log.info("订单已转移: orderId={}, toUserId={}", order.getId(), toUserId);
    }

    /**
     * 批量创建订单 - 编程式事务示例
     *
     * <p>演示 {@link TransactionTemplate} 的使用：
     * 将所有订单创建操作包裹在一个事务中，对每个请求捕获并记录异常以允许继续处理后续订单。
     * 注意：若事务整体失败，所有操作将一同回滚；若成功提交，则所有成功创建的订单一同提交。
     */
    public List<Order> batchCreateOrders(List<OrderRequest> requests) {
        log.info("=== 批量创建订单 [编程式事务] ===");

        if (txTemplate == null) {
            log.warn("TransactionTemplate 不可用，直接执行（无事务保护）");
            List<Order> orders = new ArrayList<>();
            for (OrderRequest request : requests) {
                try {
                    Order order = createOrder(request.userId, request.productId, request.quantity);
                    orders.add(order);
                } catch (Exception e) {
                    log.error("创建订单失败: {}, 继续处理下一个", e.getMessage());
                }
            }
            log.info("批量订单创建完成: 成功 {} 个", orders.size());
            return orders;
        }

        return txTemplate.execute(() -> {
            List<Order> orders = new ArrayList<>();

            for (OrderRequest request : requests) {
                try {
                    Order order = createOrder(request.userId, request.productId, request.quantity);
                    orders.add(order);
                } catch (Exception e) {
                    log.error("创建订单失败: {}, 继续处理下一个", e.getMessage());
                }
            }

            log.info("批量订单创建完成: 成功 {} 个", orders.size());
            return orders;
        });
    }

    public Order getOrderById(long id) {
        return orderStore.get(id);
    }

    public List<Order> getAllOrders() {
        return new ArrayList<>(orderStore.values());
    }

    public List<Order> getOrdersByUserId(long userId) {
        List<Order> userOrders = new ArrayList<>();
        for (Order order : orderStore.values()) {
            if (order.getUserId() == userId) {
                userOrders.add(order);
            }
        }
        return userOrders;
    }

    /**
     * 订单请求 DTO
     */
    public static class OrderRequest {
        public long userId;
        public long productId;
        public int quantity;

        public OrderRequest(long userId, long productId, int quantity) {
            this.userId = userId;
            this.productId = productId;
            this.quantity = quantity;
        }
    }
}

