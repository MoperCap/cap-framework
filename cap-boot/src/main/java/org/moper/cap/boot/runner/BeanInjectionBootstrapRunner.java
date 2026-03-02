package org.moper.cap.boot.runner;

import org.moper.cap.boot.interceptor.BeanInjectionInterceptor;
import org.moper.cap.core.annotation.RunnerMeta;
import org.moper.cap.core.context.BootstrapContext;
import org.moper.cap.core.runner.BootstrapRunner;
import org.moper.cap.core.runner.RunnerType;

/**
 * 注册 {@link BeanInjectionInterceptor} 的引导器。
 */
@RunnerMeta(type = RunnerType.KERNEL, order = 20, name = "BeanInjectionBootstrapRunner", description = "Registers BeanInjectionInterceptor for @Inject field injection")
public class BeanInjectionBootstrapRunner implements BootstrapRunner {

    @Override
    public void initialize(BootstrapContext context) throws Exception {
        context.getBeanContainer().addBeanInterceptor(
                new BeanInjectionInterceptor(context.getBeanContainer()));
    }
}
