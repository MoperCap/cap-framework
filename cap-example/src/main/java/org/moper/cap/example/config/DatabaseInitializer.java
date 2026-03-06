package org.moper.cap.example.config;

import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 数据库初始化器
 *
 * <p>在应用启动时创建必要的数据库表结构并插入初始数据。
 */
@Slf4j
public class DatabaseInitializer {

    private final DataSource dataSource;
    private boolean initialized = false;

    public DatabaseInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
        this.initialize();
    }

    /**
     * 初始化数据库。
     */
    private void initialize() {
        synchronized (this) {
            if (initialized) {
                return;
            }

            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {

                // 创建用户表
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS users (" +
                    "  id BIGINT PRIMARY KEY," +
                    "  name VARCHAR(100) NOT NULL," +
                    "  email VARCHAR(100)," +
                    "  created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")"
                );
                log.info("用户表创建成功");

                // 创建商品表
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS products (" +
                    "  id BIGINT PRIMARY KEY," +
                    "  name VARCHAR(100) NOT NULL," +
                    "  price DECIMAL(10, 2)," +
                    "  stock INT DEFAULT 0," +
                    "  created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")"
                );
                log.info("商品表创建成功");

                // 创建订单表
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS orders (" +
                    "  id BIGINT PRIMARY KEY," +
                    "  user_id BIGINT NOT NULL," +
                    "  product_id BIGINT NOT NULL," +
                    "  quantity INT NOT NULL," +
                    "  created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "  FOREIGN KEY (user_id) REFERENCES users(id)," +
                    "  FOREIGN KEY (product_id) REFERENCES products(id)" +
                    ")"
                );
                log.info("订单表创建成功");

                insertInitialData(stmt);

                initialized = true;
                log.info("数据库初始化完成");
            } catch (SQLException e) {
                log.error("数据库初始化失败", e);
                throw new RuntimeException("Failed to initialize database", e);
            }
        }
    }

    /**
     * 插入初始数据。
     */
    private void insertInitialData(Statement stmt) throws SQLException {
        // 清空现有数据
        stmt.execute("DELETE FROM orders");
        stmt.execute("DELETE FROM products");
        stmt.execute("DELETE FROM users");

        // 插入用户数据
        stmt.execute("INSERT INTO users VALUES (1, 'Alice', 'alice@example.com', CURRENT_TIMESTAMP)");
        stmt.execute("INSERT INTO users VALUES (2, 'Bob', 'bob@example.com', CURRENT_TIMESTAMP)");
        stmt.execute("INSERT INTO users VALUES (3, 'Charlie', 'charlie@example.com', CURRENT_TIMESTAMP)");
        log.info("已插入 3 个用户");

        // 插入商品数据
        stmt.execute("INSERT INTO products VALUES (1, 'Laptop', 999.99, 100, CURRENT_TIMESTAMP)");
        stmt.execute("INSERT INTO products VALUES (2, 'Mouse', 29.99, 500, CURRENT_TIMESTAMP)");
        stmt.execute("INSERT INTO products VALUES (3, 'Keyboard', 79.99, 200, CURRENT_TIMESTAMP)");
        log.info("已插入 3 个商品");
    }
}
