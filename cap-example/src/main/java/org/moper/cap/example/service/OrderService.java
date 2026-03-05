package org.moper.cap.example.service;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.bean.annotation.Inject;
import org.moper.cap.example.model.Order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Capper
public class OrderService {

    @Inject
    UserService userService;

    @Inject
    ProductService productService;

    private final Map<Long, Order> orderStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public Order createOrder(long userId, long productId, int quantity) {
        log.info("创建订单: userId={}, productId={}, quantity={}", userId, productId, quantity);

        if (!userService.userExists(userId)) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (!productService.productExists(productId)) {
            throw new IllegalArgumentException("商品不存在");
        }

        if (!productService.decrementStock(productId, quantity)) {
            throw new IllegalArgumentException("库存不足");
        }

        Order order = new Order();
        order.setId(idGenerator.incrementAndGet());
        order.setUserId(userId);
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setCreateTime(LocalDateTime.now());

        orderStore.put(order.getId(), order);
        log.info("订单创建成功: orderId={}", order.getId());
        return order;
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
}

