package org.moper.cap.example.bean;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.bean.annotation.Inject;
import org.moper.cap.example.service.ProductService;
import org.moper.cap.example.service.UserService;

/**
 * 支付服务，演示通过 {@link Inject} 显式指定 Bean 名称进行依赖注入。
 *
 * <p>通过 {@code @Inject("product")} 注入别名为 {@code "product"} 的
 * {@link ProductService} Bean，而非默认的类型匹配。
 */
@Slf4j
@Capper
public class PaymentService {

    @Inject
    private UserService userService;

    @Inject("product")
    private ProductService productService;

    public String pay(long userId, long productId, double amount) {
        String user = userService.findUser(userId);
        String product = productService.findProduct(productId);
        String result = "Payment[" + user + " paid " + amount + " for " + product + "]";
        log.info("Processed: {}", result);
        return result;
    }
}
