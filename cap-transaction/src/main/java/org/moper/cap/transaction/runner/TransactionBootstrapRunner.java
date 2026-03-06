package org.moper.cap.transaction.runner;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.core.annotation.RunnerMeta;
import org.moper.cap.core.context.BootstrapContext;
import org.moper.cap.core.runner.BootstrapRunner;
import org.moper.cap.core.runner.RunnerType;
import org.moper.cap.transaction.interceptor.TransactionBeanInterceptor;

/**
 * 事务启动器 - 完全独立，不依赖任何实现
 *
 * <p>职责：
 * <ol>
 *   <li>注册 {@link TransactionBeanInterceptor}</li>
 *   <li>拦截所有标注 {@code @Transactional} 的方法</li>
 *   <li>在运行时查找 TransactionManager 实现</li>
 * </ol>
 *
 * <p>设计原则：
 * <ul>
 *   <li>不依赖 DataSource</li>
 *   <li>不依赖 JdbcTransactionManager</li>
 *   <li>不依赖任何具体实现</li>
 *   <li>由具体的实现（如 cap-data）提供 TransactionManager</li>
 * </ul>
 */
@Slf4j
@RunnerMeta(
    type = RunnerType.FEATURE,
    order = 380,
    name = "CapTransactionBootstrapRunner",
    description = "注册事务拦截器 - 完全独立，不依赖具体实现"
)
public class TransactionBootstrapRunner implements BootstrapRunner {

    @Override
    public void initialize(BootstrapContext context) throws Exception {
        log.info("=== 初始化事务模块 ===");

        BeanContainer container = context.getBeanContainer();

        try {
            // 创建并注册 TransactionBeanInterceptor
            // 注意：此时不需要 TransactionManager，在运行时延迟获取
            TransactionBeanInterceptor interceptor = new TransactionBeanInterceptor(container);

            container.addBeanInterceptor(interceptor);

            log.info("✅ 事务模块初始化完成");
            log.info("   - TransactionBeanInterceptor 已注册");
            log.info("   - 现在可以使用 @Transactional 注解");
            log.info("   💡 注意：事务功能依赖于 TransactionManager 的具体实现");
            log.info("           请确保已在应用中配置 TransactionManager 实现");
        } catch (Exception e) {
            log.error("❌ 事务模块初始化失败", e);
            throw e;
        }
    }
}
