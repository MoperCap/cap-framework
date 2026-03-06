package org.moper.cap.example.config;

import lombok.extern.slf4j.Slf4j;
import org.h2.jdbcx.JdbcDataSource;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.data.transaction.JdbcTransactionManager;
import org.moper.cap.transaction.manager.TransactionManager;

import javax.sql.DataSource;

/**
 * 数据源配置
 *
 * <p>使用 H2 内存数据库作为演示，可以快速启动和测试事务功能。
 */
@Slf4j
@Capper
public class DataSourceConfig {

    /**
     * 创建并配置 H2 DataSource。
     *
     * @return H2 内存数据库的 DataSource
     */
    @Capper
    public DataSource dataSource() {
        log.info("=== 创建 H2 内存数据源 ===");

        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:capdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        ds.setUser("sa");
        ds.setPassword("");

        log.info("DataSource 配置完成: URL={}", ds.getURL());

        return ds;
    }

    /**
     * 创建 TransactionManager Bean。
     *
     * <p>使用 cap-data 模块提供的 {@link JdbcTransactionManager} 实现。
     *
     * @param dataSource 数据源
     * @return JDBC 事务管理器
     */
    @Capper
    public TransactionManager transactionManager(DataSource dataSource) {
        log.info("=== 创建 TransactionManager ===");
        TransactionManager txManager = new JdbcTransactionManager(dataSource);
        log.info("TransactionManager 创建完成");
        return txManager;
    }

    /**
     * 初始化数据库表结构。
     *
     * <p>在 DataSource 创建后执行 SQL 初始化脚本。
     *
     * @param dataSource 数据源
     * @return 数据库初始化器
     */
    @Capper
    public DatabaseInitializer databaseInitializer(DataSource dataSource) {
        log.info("=== 初始化数据库表结构 ===");
        return new DatabaseInitializer(dataSource);
    }
}
