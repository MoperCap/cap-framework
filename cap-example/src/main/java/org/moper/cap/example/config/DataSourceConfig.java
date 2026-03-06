package org.moper.cap.example.config;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.Capper;

import javax.sql.DataSource;

/**
 * 数据源相关配置
 *
 * <p>DataSource 和 TransactionManager 的创建已由 cap-data 模块的
 * {@code DataSourceFactory} 和 {@code TransactionManagerFactory} 自动处理，
 * 通过 application.yaml 中的 {@code database.*} 属性进行配置。
 *
 * <p>本配置类仅负责应用特定的数据库初始化工作。
 */
@Slf4j
@Capper
public class DataSourceConfig {

    /**
     * 初始化数据库表结构。
     *
     * <p>在 DataSource 创建后执行 SQL 初始化脚本。
     *
     * @param dataSource 数据源（由 DataSourceFactory 创建）
     * @return 数据库初始化器
     */
    @Capper
    public DatabaseInitializer databaseInitializer(DataSource dataSource) {
        log.info("=== 初始化数据库表结构 ===");
        return new DatabaseInitializer(dataSource);
    }
}

