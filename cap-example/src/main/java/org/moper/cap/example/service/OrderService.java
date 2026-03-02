package org.moper.cap.example.service;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.bean.annotation.Inject;

/**
 * 订单服务，演示字段注入（{@link Inject} 标注字段）。
 *
 * <p>依赖 {@link UserService} 和 {@link ProductService}，
 * 均通过 {@link Inject} 字段注入由容器自动完成。
 */
@Slf4j
@Capper
public class OrderService {

    @Inject
    private UserService userService;

    @Inject
    private ProductService productService;

    public String createOrder(long userId, long productId) {
        String user = userService.findUser(userId);
        String product = productService.findProduct(productId);
        String order = "Order[" + user + " -> " + product + "]";
        log.info("Created: {}", order);
        return order;
    }
}
