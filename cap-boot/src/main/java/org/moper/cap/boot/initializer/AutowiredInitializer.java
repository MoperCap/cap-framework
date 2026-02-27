package org.moper.cap.boot.initializer;

import org.moper.cap.boot.interceptor.AutowiredBeanInterceptor;
import org.moper.cap.context.initializer.Initializer;
import org.moper.cap.context.initializer.InitializerType;
import org.moper.cap.context.context.BootstrapContext;

/**
 * 注册 {@link AutowiredBeanInterceptor} 的构造机
 */
public class AutowiredInitializer extends Initializer {

    public AutowiredInitializer() {
        super(InitializerType.KERNEL, 20, "AutowiredInitializer",
                "Registers AutowiredBeanInterceptor for @Autowired and @Value injection");
    }

    @Override
    public void initialize(BootstrapContext context) throws Exception {
        context.getBeanContainer().addBeanInterceptor(
                new AutowiredBeanInterceptor(context.getBeanContainer(), context.getPropertyOfficer()));
    }
}
