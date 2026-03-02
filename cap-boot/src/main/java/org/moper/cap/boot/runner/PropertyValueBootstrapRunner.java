package org.moper.cap.boot.runner;

import org.moper.cap.boot.interceptor.PropertyValueBeanInterceptor;
import org.moper.cap.core.annotation.RunnerMeta;
import org.moper.cap.core.context.BootstrapContext;
import org.moper.cap.core.runner.BootstrapRunner;
import org.moper.cap.core.runner.RunnerType;

/**
 * 注册 {@link PropertyValueBeanInterceptor} 的引导器。
 */
@RunnerMeta(type = RunnerType.KERNEL, order = 325, description = "Registers PropertyValueBeanInterceptor for @Value field injection")
public class PropertyValueBootstrapRunner implements BootstrapRunner {

    @Override
    public void initialize(BootstrapContext context) throws Exception {
        context.getBeanContainer().addBeanInterceptor(
                new PropertyValueBeanInterceptor(context.getPropertyOfficer()));
    }
}
