package org.moper.cap.transaction.runner;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.core.annotation.RunnerMeta;
import org.moper.cap.core.context.BootstrapContext;
import org.moper.cap.core.runner.BootstrapRunner;
import org.moper.cap.core.runner.RunnerType;
import org.moper.cap.transaction.interceptor.TransactionBeanInterceptor;
import org.moper.cap.transaction.manager.JdbcTransactionManager;
import org.moper.cap.transaction.manager.TransactionManager;

import javax.sql.DataSource;

/**
 * 事务启动器 - 作为 BootstrapRunner 初始化事务系统
 *
 * <p>职责：
 * <ol>
 *   <li>从 BeanContainer 中获取 {@link DataSource}</li>
 *   <li>创建并注册 {@link TransactionManager}</li>
 *   <li>创建 {@link TransactionBeanInterceptor} 并注册到容器</li>
 * </ol>
 *
 * <p>若容器中没有 {@link DataSource}，本 Runner 会跳过初始化并打印警告日志。
 */
@Slf4j
@RunnerMeta(type = RunnerType.FEATURE, order = 380, description = "初始化事务系统：创建 TransactionManager 并注册 TransactionBeanInterceptor")
public class TransactionBootstrapRunner implements BootstrapRunner {

    @Override
    public void initialize(BootstrapContext context) throws Exception {
        log.info("=== 初始化事务模块 ===");

        BeanContainer container = context.getBeanContainer();

        // 1. 获取 DataSource
        DataSource dataSource = obtainDataSource(container);
        if (dataSource == null) {
            log.warn("容器中未找到 DataSource，事务功能将被跳过");
            log.warn("提示：请在应用中配置 DataSource Bean");
            log.warn("   例如：在 @Capper 标注的类中创建 DataSource 工厂方法");
            return;
        }

        // 2. 创建 TransactionManager 并注册到容器
        TransactionManager txManager = new JdbcTransactionManager(dataSource);
        container.registerSingleton("transactionManager", txManager);
        log.info("事务管理器已注册");

        // 3. 注册 TransactionBeanInterceptor
        container.addBeanInterceptor(new TransactionBeanInterceptor(txManager));
        log.info("事务 Bean 拦截器已注册（order=500，在 AOP 拦截器之后执行）");
    }

    private DataSource obtainDataSource(BeanContainer container) {
        try {
            return container.getBean(DataSource.class);
        } catch (Exception e) {
            log.debug("容器中未找到 DataSource: {}", e.getMessage());
            return null;
        }
    }
}
