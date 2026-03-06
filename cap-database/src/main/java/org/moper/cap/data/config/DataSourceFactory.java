package org.moper.cap.data.config;

import lombok.extern.slf4j.Slf4j;
import org.h2.jdbcx.JdbcDataSource;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.property.annotation.Value;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

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
     * 创建 MySQL DataSource。
     */
    private DataSource createMySqlDataSource(String url, String username, String password) {
        return new DriverManagerDataSource(url, username, password);
    }

    /**
     * 创建 PostgreSQL DataSource。
     */
    private DataSource createPostgresDataSource(String url, String username, String password) {
        return new DriverManagerDataSource(url, username, password);
    }

    /**
     * 基于 DriverManager 的简单 DataSource 实现，适用于开发/测试环境。
     *
     * <p>每次调用 {@link #getConnection()} 时通过 {@link DriverManager} 创建新连接。
     * 生产环境建议使用 HikariCP 等连接池。
     */
    private static class DriverManagerDataSource implements DataSource {

        private final String url;
        private final String username;
        private final String password;
        private PrintWriter logWriter;

        DriverManagerDataSource(String url, String username, String password) {
            this.url = url;
            this.username = username;
            this.password = password;
        }

        @Override
        public Connection getConnection() throws SQLException {
            return DriverManager.getConnection(url, username, password);
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            return DriverManager.getConnection(url, username, password);
        }

        @Override
        public PrintWriter getLogWriter() {
            return logWriter;
        }

        @Override
        public void setLogWriter(PrintWriter out) {
            this.logWriter = out;
        }

        @Override
        public void setLoginTimeout(int seconds) {
            DriverManager.setLoginTimeout(seconds);
        }

        @Override
        public int getLoginTimeout() {
            return DriverManager.getLoginTimeout();
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException("getParentLogger not supported");
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            throw new SQLException("Not a wrapper for " + iface);
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) {
            return false;
        }
    }
}
