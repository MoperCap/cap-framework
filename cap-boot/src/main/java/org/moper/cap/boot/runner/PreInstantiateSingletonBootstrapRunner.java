package org.moper.cap.boot.runner;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.core.annotation.RunnerMeta;
import org.moper.cap.core.context.BootstrapContext;
import org.moper.cap.core.runner.BootstrapRunner;
import org.moper.cap.core.runner.RunnerType;

@Slf4j
@RunnerMeta(type = RunnerType.KERNEL, order = 500, description = "Pre-instantiates all non-lazy singleton beans to optimize runtime latency")
public class PreInstantiateSingletonBootstrapRunner implements BootstrapRunner {

    /**
     * 框架初始化阶段执行器 </br>
     *
     * @param context 框架初始化阶段系统上下文
     * @throws Exception 执行过程中可能抛出的异常
     */
    @Override
    public void initialize(BootstrapContext context) throws Exception {
        context.getBeanContainer().preInstantiateSingletons();
        log.info("Eagerly instantiated all singleton beans");
    }
}
