package org.moper.cap.example.bean;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.bean.annotation.Inject;
import org.moper.cap.example.model.Product;
import org.moper.cap.example.model.User;
import org.moper.cap.example.service.ProductService;
import org.moper.cap.example.service.UserService;

/**
 * 支付服务，演示通过 {@link Inject} 依赖注入 UserService 和 ProductService。
 */
@Slf4j
@Capper
public class PaymentService {

    @Inject
    private UserService userService;

    @Inject
    private ProductService productService;

    public String pay(long userId, long productId, double amount) {
        User user = userService.getUserById(userId);
        Product product = productService.getProductById(productId);
        String userName = user != null ? user.getName() : "User#" + userId;
        String productName = product != null ? product.getName() : "Product#" + productId;
        String result = "Payment[" + userName + " paid " + amount + " for " + productName + "]";
        log.info("Processed: {}", result);
        return result;
    }
}

