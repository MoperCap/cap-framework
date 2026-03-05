package org.moper.cap.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.bean.annotation.Inject;
import org.moper.cap.example.model.ApiResponse;
import org.moper.cap.example.model.Order;
import org.moper.cap.example.service.OrderService;
import org.moper.cap.web.annotation.controller.RestController;
import org.moper.cap.web.annotation.mapping.GetMapping;
import org.moper.cap.web.annotation.mapping.PostMapping;
import org.moper.cap.web.annotation.mapping.RequestMapping;
import org.moper.cap.web.annotation.request.PathVariable;
import org.moper.cap.web.annotation.request.RequestBody;

import java.util.List;
import java.util.Map;

@Slf4j
@Capper
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Inject
    OrderService orderService;

    /**
     * 获取所有订单
     * GET /api/orders
     */
    @GetMapping
    public ApiResponse<List<Order>> getAllOrders() {
        log.info("获取所有订单");
        List<Order> orders = orderService.getAllOrders();
        return ApiResponse.success("获取订单列表成功", orders);
    }

    /**
     * 获取指定订单
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<Order> getOrderById(@PathVariable long id) {
        log.info("获取订单: id={}", id);
        Order order = orderService.getOrderById(id);
        if (order == null) {
            return ApiResponse.error("订单不存在");
        }
        return ApiResponse.success("获取订单成功", order);
    }

    /**
     * 获取用户的所有订单
     * GET /api/orders/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<List<Order>> getOrdersByUserId(@PathVariable long userId) {
        log.info("获取用户订单: userId={}", userId);
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return ApiResponse.success("获取用户订单成功", orders);
    }

    /**
     * 创建订单
     * POST /api/orders
     * Body: {"userId": 1, "productId": 1, "quantity": 2}
     */
    @PostMapping
    public ApiResponse<Order> createOrder(@RequestBody Map<String, Object> request) {
        Object userIdObj = request.get("userId");
        Object productIdObj = request.get("productId");
        Object quantityObj = request.get("quantity");

        if (userIdObj == null || productIdObj == null || quantityObj == null) {
            return ApiResponse.error("缺少必填字段: userId, productId, quantity");
        }

        long userId = ((Number) userIdObj).longValue();
        long productId = ((Number) productIdObj).longValue();
        int quantity = ((Number) quantityObj).intValue();

        log.info("创建订单: userId={}, productId={}, quantity={}", userId, productId, quantity);

        try {
            Order order = orderService.createOrder(userId, productId, quantity);
            return ApiResponse.success("创建订单成功", order);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
