package org.moper.cap.transaction.runner;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.core.annotation.RunnerMeta;
import org.moper.cap.core.context.BootstrapContext;
import org.moper.cap.core.runner.BootstrapRunner;
import org.moper.cap.core.runner.RunnerType;
import org.moper.cap.transaction.aspect.TransactionAspect;

/**
 * 事务模块启动器，负责将 {@link TransactionAspect} 注册到 Bean 容器。
 *
 * <p>必须在 AOP 启动器（order=400）之前运行（本启动器 order=395），以确保
 * {@link org.moper.cap.aop.runner.AopBootstrapRunner} 扫描 {@link org.moper.cap.aop.annotation.Aspect}
 * Bean 时能找到 {@link TransactionAspect}，并为其生成对应的 {@link org.moper.cap.aop.proxy.Advisor}。
 *
 * <p>TransactionAspect 以单例方式注册，持有对 {@link BeanContainer} 的引用，
 * 在运行时懒加载 {@link org.moper.cap.transaction.manager.TransactionManager} Bean。
 */
@Slf4j
@RunnerMeta(
        type = RunnerType.FEATURE,
        order = 395,
        name = "TransactionBootstrapRunner",
        description = "Registers TransactionAspect as a singleton bean before AOP scanner runs"
)
public class TransactionBootstrapRunner implements BootstrapRunner {

    @Override
    public void initialize(BootstrapContext context) throws Exception {
        BeanContainer beanContainer = context.getBeanContainer();
        TransactionAspect transactionAspect = new TransactionAspect(beanContainer);
        beanContainer.registerSingleton("transactionAspect", transactionAspect);
        log.debug("TransactionAspect 已注册到 Bean 容器");
    }
}
