package org.moper.cap.boot.runner;

import org.moper.cap.boot.interceptor.AutowiredBeanInterceptor;
import org.moper.cap.core.annotation.RunnerMeta;
import org.moper.cap.core.runner.BootstrapRunner;
import org.moper.cap.core.runner.RunnerType;
import org.moper.cap.core.context.BootstrapContext;

/**
 * 注册 {@link AutowiredBeanInterceptor} 的构造机
 */
@RunnerMeta(type = RunnerType.KERNEL, order = 20, name = "CapAutowiredBootstrapRunner", description = "Registers AutowiredBeanInterceptor for @Autowired and @Value injection")
public class AutowiredBootstrapRunner implements BootstrapRunner {

    @Override
    public void initialize(BootstrapContext context) throws Exception {
        context.getBeanContainer().addBeanInterceptor(
                new AutowiredBeanInterceptor(context.getBeanContainer(), context.getPropertyOfficer()));
    }
}
