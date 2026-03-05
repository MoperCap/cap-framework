package org.moper.cap.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.moper.cap.example.model.Product;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ProductServiceTest {

    private ProductService service;

    @BeforeEach
    public void setUp() {
        service = new ProductService();
    }

    @Test
    public void testCreateProduct() {
        Product product = new Product("Monitor", 399.99, 100);
        Product created = service.createProduct(product);

        assertNotNull(created.getId());
        assertEquals("Monitor", created.getName());
        assertEquals(399.99, created.getPrice());
    }

    @Test
    public void testGetProductById() {
        Product product = service.getProductById(1);
        assertNotNull(product);
        assertEquals("Laptop", product.getName());
    }

    @Test
    public void testGetAllProducts() {
        List<Product> products = service.getAllProducts();
        assertTrue(products.size() >= 3);
    }

    @Test
    public void testDecrementStock_Success() {
        Product product = service.getProductById(1);
        int originalStock = product.getStock();

        boolean success = service.decrementStock(1, 10);
        assertTrue(success);

        Product updated = service.getProductById(1);
        assertEquals(originalStock - 10, updated.getStock());
    }

    @Test
    public void testDecrementStock_InsufficientStock() {
        boolean success = service.decrementStock(1, 10000);
        assertFalse(success);
    }

    @Test
    public void testProductExists() {
        assertTrue(service.productExists(1));
        assertFalse(service.productExists(999));
    }
}
