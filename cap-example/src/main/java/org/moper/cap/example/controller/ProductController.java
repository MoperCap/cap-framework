package org.moper.cap.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.bean.annotation.Inject;
import org.moper.cap.example.model.ApiResponse;
import org.moper.cap.example.model.Product;
import org.moper.cap.example.service.ProductService;
import org.moper.cap.web.annotation.controller.RestController;
import org.moper.cap.web.annotation.mapping.GetMapping;
import org.moper.cap.web.annotation.mapping.PostMapping;
import org.moper.cap.web.annotation.mapping.RequestMapping;
import org.moper.cap.web.annotation.request.PathVariable;
import org.moper.cap.web.annotation.request.RequestBody;

import java.util.List;

@Slf4j
@Capper
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Inject
    ProductService productService;

    /**
     * 获取所有商品
     * GET /api/products
     */
    @GetMapping
    public ApiResponse<List<Product>> getAllProducts() {
        log.info("获取所有商品");
        List<Product> products = productService.getAllProducts();
        return ApiResponse.success("获取商品列表成功", products);
    }

    /**
     * 获取指定商品
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<Product> getProductById(@PathVariable("id") long id) {
        log.info("获取商品: id={}", id);
        Product product = productService.getProductById(id);
        if (product == null) {
            return ApiResponse.error("商品不存在");
        }
        return ApiResponse.success("获取商品成功", product);
    }

    /**
     * 创建商品
     * POST /api/products
     */
    @PostMapping
    public ApiResponse<Product> createProduct(@RequestBody Product product) {
        log.info("创建商品: name={}", product.getName());
        Product created = productService.createProduct(product);
        return ApiResponse.success("创建商品成功", created);
    }
}
