package org.moper.cap.boot.runner;

import org.moper.cap.boot.interceptor.PropertySubscriptionBeanInterceptor;
import org.moper.cap.core.annotation.RunnerMeta;
import org.moper.cap.core.context.BootstrapContext;
import org.moper.cap.core.runner.BootstrapRunner;
import org.moper.cap.core.runner.RunnerType;

/**
 * 注册 {@link PropertySubscriptionBeanInterceptor} 的引导器。
 */
@RunnerMeta(type = RunnerType.KERNEL, order = 370, description = "Registers PropertySubscriptionBeanInterceptor for @Subscriber field subscription")
public class PropertySubscriptionBootstrapRunner implements BootstrapRunner {

    @Override
    public void initialize(BootstrapContext context) throws Exception {
        context.getBeanContainer().addBeanInterceptor(
                new PropertySubscriptionBeanInterceptor(
                        context.getPropertyOfficer(),
                        context.getBeanContainer()));
    }
}
