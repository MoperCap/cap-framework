package org.moper.cap.example.service;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.example.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Capper
public class ProductService {

    private final Map<Long, Product> productStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public ProductService() {
        log.info("ProductService 初始化");
        createProduct(new Product("Laptop", 1299.99, 50));
        createProduct(new Product("Mouse", 29.99, 200));
        createProduct(new Product("Keyboard", 99.99, 150));
    }

    public Product getProductById(long id) {
        log.debug("获取商品: {}", id);
        return productStore.get(id);
    }

    public List<Product> getAllProducts() {
        log.debug("获取所有商品");
        return new ArrayList<>(productStore.values());
    }

    public Product createProduct(Product product) {
        long id = idGenerator.incrementAndGet();
        product.setId(id);
        productStore.put(id, product);
        log.info("创建商品: id={}, name={}, price={}", id, product.getName(), product.getPrice());
        return product;
    }

    public boolean productExists(long id) {
        return productStore.containsKey(id);
    }

    public boolean decrementStock(long id, int quantity) {
        Product product = productStore.get(id);
        if (product == null || product.getStock() < quantity) {
            return false;
        }
        product.setStock(product.getStock() - quantity);
        log.info("减少商品库存: id={}, quantity={}", id, quantity);
        return true;
    }
}

