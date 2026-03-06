package org.moper.cap.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.moper.cap.example.model.Order;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrderServiceTest {

    private OrderService orderService;
    private UserService userService;
    private ProductService productService;

    @BeforeEach
    public void setUp() {
        userService = new UserService();
        productService = new ProductService();
        orderService = new OrderService();
        orderService.userService = userService;
        orderService.productService = productService;
    }

    @Test
    public void testCreateOrder_Success() {
        Order order = orderService.createOrder(1, 1, 5);

        assertNotNull(order.getId());
        assertEquals(1, order.getUserId());
        assertEquals(1, order.getProductId());
        assertEquals(5, order.getQuantity());
        assertNotNull(order.getCreateTime());
    }

    @Test
    public void testCreateOrder_UserNotFound() {
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(999, 1, 5);
        });
    }

    @Test
    public void testCreateOrder_ProductNotFound() {
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(1, 999, 5);
        });
    }

    @Test
    public void testCreateOrder_InsufficientStock() {
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(1, 1, 10000);
        });
    }

    @Test
    public void testGetOrderById() {
        Order created = orderService.createOrder(1, 1, 3);
        Order retrieved = orderService.getOrderById(created.getId());

        assertNotNull(retrieved);
        assertEquals(created.getId(), retrieved.getId());
    }

    @Test
    public void testGetAllOrders() {
        orderService.createOrder(1, 1, 2);
        orderService.createOrder(1, 2, 3);

        List<Order> orders = orderService.getAllOrders();
        assertTrue(orders.size() >= 2);
    }

    @Test
    public void testGetOrdersByUserId() {
        orderService.createOrder(1, 1, 2);
        orderService.createOrder(1, 2, 1);
        orderService.createOrder(2, 1, 5);

        List<Order> userOrders = orderService.getOrdersByUserId(1);
        assertEquals(2, userOrders.size());
    }
}
