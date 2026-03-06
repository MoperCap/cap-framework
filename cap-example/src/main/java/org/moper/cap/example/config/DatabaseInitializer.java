package org.moper.cap.example.config;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.Capper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 数据库初始化器 - 验证数据库连接和表结构
 *
 * <p>在应用启动时：
 * <ol>
 *   <li>验证数据库连接是否正常</li>
 *   <li>检查必要的表是否存在</li>
 *   <li>输出数据库统计信息</li>
 * </ol>
 */
@Slf4j
@Capper
public class DatabaseInitializer {

    private final DataSource dataSource;
    private volatile boolean initialized = false;

    public DatabaseInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
        this.initialize();
    }

    /**
     * 初始化数据库连接和信息检查。
     */
    private void initialize() {
        if (initialized) {
            return;
        }

        synchronized (this) {
            if (initialized) {
                return;
            }

            try {
                log.info("=== 初始化数据库连接 ===");

                try (Connection conn = dataSource.getConnection()) {
                    log.info("✅ 数据库连接成功");
                    logDatabaseInfo(conn);
                    checkTableStructure(conn);
                }

                initialized = true;
                log.info("✅ 数据库初始化完成");
            } catch (Exception e) {
                log.error("❌ 数据库初始化失败", e);
                throw new RuntimeException("Failed to initialize database", e);
            }
        }
    }

    /**
     * 输出数据库信息。
     */
    private void logDatabaseInfo(Connection conn) throws Exception {
        String dbProductName = conn.getMetaData().getDatabaseProductName();
        String dbProductVersion = conn.getMetaData().getDatabaseProductVersion();
        String dbUrl = conn.getMetaData().getURL();
        String dbUser = conn.getMetaData().getUserName();

        log.info("数据库信息：");
        log.info("  - 产品: {}", dbProductName);
        log.info("  - 版本: {}", dbProductVersion);
        log.info("  - URL: {}", dbUrl);
        log.info("  - 用户: {}", dbUser);
    }

    /**
     * 检查必要的表是否存在。
     */
    private void checkTableStructure(Connection conn) throws Exception {
        String[] requiredTables = {
            "users",
            "products",
            "orders",
            "order_items",
            "payments",
            "shopping_carts",
            "inventory_logs"
        };

        log.info("检查表结构：");

        try (Statement stmt = conn.createStatement()) {
            for (String tableName : requiredTables) {
                if (!isValidTableName(tableName)) {
                    log.warn("  ⚠️  {} (无效的表名，已跳过)", tableName);
                    continue;
                }

                String sql = "SELECT COUNT(*) FROM information_schema.tables " +
                           "WHERE table_schema = DATABASE() AND table_name = '" + tableName + "'";

                try (ResultSet rs = stmt.executeQuery(sql)) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        String countSql = "SELECT COUNT(*) FROM `" + tableName + "`";
                        try (ResultSet countRs = stmt.executeQuery(countSql)) {
                            if (countRs.next()) {
                                long count = countRs.getLong(1);
                                log.info("  ✅ {} (数据行数: {})", tableName, count);
                            }
                        }
                    } else {
                        log.warn("  ⚠️  {} (表不存在)", tableName);
                    }
                }
            }
        }
    }

    /**
     * 校验表名合法性，只允许字母、数字和下划线，防止 SQL 注入。
     */
    private boolean isValidTableName(String tableName) {
        return tableName != null && tableName.matches("[a-zA-Z0-9_]+");
    }
}
