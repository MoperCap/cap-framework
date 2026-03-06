package org.moper.cap.data.config;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.data.transaction.JdbcTransactionManager;
import org.moper.cap.transaction.manager.TransactionManager;
import org.moper.cap.transaction.template.TransactionTemplate;

import javax.sql.DataSource;

/**
 * 事务管理器工厂 - 根据 DataSource 创建 TransactionManager 和 TransactionTemplate
 *
 * <p>由 cap-data 模块自动提供，用户无需手动创建这些 Bean。
 * 只要引入 cap-data 并在扫描路径中包含 {@code org.moper.cap.data.config}，
 * 框架即自动完成事务管理器的配置。
 */
@Slf4j
@Capper
public class TransactionManagerFactory {

    /**
     * 创建 TransactionManager。
     *
     * @param dataSource 数据源
     * @return JDBC 事务管理器实现
     */
    @Capper
    public TransactionManager transactionManager(DataSource dataSource) {
        log.info("=== 创建事务管理器 ===");

        TransactionManager txManager = new JdbcTransactionManager(dataSource);

        log.info("✅ TransactionManager 创建成功: {}", txManager.getClass().getSimpleName());
        return txManager;
    }

    /**
     * 创建 TransactionTemplate - 用于编程式事务。
     *
     * @param transactionManager 事务管理器
     * @return 编程式事务模板
     */
    @Capper
    public TransactionTemplate transactionTemplate(TransactionManager transactionManager) {
        log.info("=== 创建 TransactionTemplate ===");

        TransactionTemplate template = new TransactionTemplate(transactionManager);

        log.info("✅ TransactionTemplate 创建成功");
        return template;
    }
}
