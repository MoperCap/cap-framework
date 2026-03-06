package org.moper.cap.transaction.runner;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.core.annotation.RunnerMeta;
import org.moper.cap.core.context.BootstrapContext;
import org.moper.cap.core.runner.BootstrapRunner;
import org.moper.cap.core.runner.RunnerType;
import org.moper.cap.transaction.aspect.TransactionAspect;

/**
 * 事务启动器 - 注册事务 Aspect，基于 AOP 框架处理 @Transactional 方法
 *
 * <p>职责：
 * <ol>
 *   <li>在 AOP 框架（order=400）扫描切面 Bean 之前，将 {@link TransactionAspect} 注册到容器</li>
 *   <li>AOP 框架自动发现并应用 {@link TransactionAspect}，为所有 {@code @Transactional} 方法创建代理</li>
 * </ol>
 *
 * <p>设计说明：
 * <ul>
 *   <li>{@link TransactionAspect} 持有 {@link BeanContainer} 引用，在运行时延迟获取 TransactionManager</li>
 *   <li>不再需要独立的 Bean 拦截器或 Javassist 代理生成器</li>
 * </ul>
 */
@Slf4j
@RunnerMeta(
    type = RunnerType.FEATURE,
    order = 380,
    name = "CapTransactionBootstrapRunner",
    description = "注册事务 Aspect - 基于 AOP 框架"
)
public class TransactionBootstrapRunner implements BootstrapRunner {

    @Override
    public void initialize(BootstrapContext context) throws Exception {
        log.info("=== 初始化事务模块 ===");

        BeanContainer container = context.getBeanContainer();

        try {
            if (!container.containsBean("transactionAspect")) {
                TransactionAspect aspect = new TransactionAspect(container);
                container.registerSingleton("transactionAspect", aspect);
            }

            log.info("✅ 事务模块初始化完成");
            log.info("   - TransactionAspect 已注册");
            log.info("   - @Transactional 注解已启用");
            log.info("   - 所有 @Transactional 方法将通过 AOP 自动代理");
        } catch (Exception e) {
            log.error("❌ 事务模块初始化失败", e);
            throw e;
        }
    }
}
