package org.moper.cap.data.config;

import lombok.extern.slf4j.Slf4j;
import org.h2.jdbcx.JdbcDataSource;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.property.annotation.Value;

import javax.sql.DataSource;

/**
 * 数据源工厂 - 根据属性配置动态创建 DataSource
 *
 * <p>不需要用户手动配置数据源 Bean，框架自动从 application.yaml 读取 {@code database.*}
 * 配置并创建相应的 DataSource。
 *
 * <p>支持的配置格式（application.yaml）：
 * <pre>
 * database:
 *   driver: com.mysql.cj.jdbc.Driver
 *   url: jdbc:mysql://localhost:3306/mydb
 *   username: root
 *   password: "123456"
 * </pre>
 *
 * <p>或使用 H2 内存数据库（开发/测试环境）：
 * <pre>
 * database:
 *   driver: org.h2.Driver
 *   url: jdbc:h2:mem:capdb;MODE=MySQL
 *   username: sa
 *   password: ""
 * </pre>
 *
 * <p>如果不配置，默认使用 H2 内存数据库。
 */
@Slf4j
@Capper
public class DataSourceFactory {

    @Value("${database.driver:org.h2.Driver}")
    private String driver;

    @Value("${database.url:jdbc:h2:mem:capdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE}")
    private String url;

    @Value("${database.username:sa}")
    private String username;

    @Value("${database.password:}")
    private String password;

    /**
     * 根据属性配置动态创建 DataSource。
     *
     * @return DataSource 实例
     */
    @Capper
    public DataSource dataSource() {
        log.info("=== 从配置动态创建 DataSource ===");
        log.info("数据源配置：");
        log.info("  - 驱动: {}", driver);
        log.info("  - URL: {}", url);
        log.info("  - 用户名: {}", username);

        DataSource ds = createDataSource(driver, url, username, password);

        log.info("✅ DataSource 创建成功");
        return ds;
    }

    /**
     * 根据驱动和配置创建具体的 DataSource 实例。
     */
    private DataSource createDataSource(String driver, String url, String username, String password) {
        try {
            Class.forName(driver);

            if (driver.contains("h2")) {
                return createH2DataSource(url, username, password);
            } else if (driver.contains("mysql")) {
                return createMySqlDataSource(url, username, password);
            } else if (driver.contains("postgresql")) {
                return createPostgresDataSource(url, username, password);
            } else {
                throw new UnsupportedOperationException("Unsupported database driver: " + driver);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Database driver not found: " + driver, e);
        }
    }

    /**
     * 创建 H2 DataSource。
     */
    private DataSource createH2DataSource(String url, String username, String password) {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL(url);
        ds.setUser(username);
        ds.setPassword(password);
        return ds;
    }

    /**
     * 创建 MySQL DataSource（可扩展）。
     */
    private DataSource createMySqlDataSource(String url, String username, String password) {
        throw new UnsupportedOperationException(
                "MySQL DataSource 尚未实现。建议引入 HikariCP 等连接池并自定义 DataSource Bean。");
    }

    /**
     * 创建 PostgreSQL DataSource（可扩展）。
     */
    private DataSource createPostgresDataSource(String url, String username, String password) {
        throw new UnsupportedOperationException(
                "PostgreSQL DataSource 尚未实现。建议引入 HikariCP 等连接池并自定义 DataSource Bean。");
    }
}
