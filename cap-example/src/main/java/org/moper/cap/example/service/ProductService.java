package org.moper.cap.example.service;

import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.bean.definition.BeanScope;

/**
 * 商品服务，演示多名称（别名）和原型作用域。
 *
 * <p>{@code names} 中第一个名称 {@code "product"} 为主名称，
 * {@code "productService"} 作为别名注册，两者均可从容器中获取同一 Bean 定义。
 * 由于作用域为 {@link BeanScope#PROTOTYPE}，每次获取都会创建新实例。
 */
@Capper(names = {"product", "productService"}, scope = BeanScope.PROTOTYPE, description = "商品服务")
public class ProductService {

    public String findProduct(long id) {
        return "Product#" + id;
    }
}
