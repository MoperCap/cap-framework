package org.moper.cap.boot.initializer;

import org.moper.cap.bootstrap.Initializer;
import org.moper.cap.bootstrap.InitializerType;
import org.moper.cap.context.BootstrapContext;
import org.moper.cap.exception.ContextException;

/**
 * 注册 {@link AutowiredBeanInterceptor} 的构造机
 */
public class AutowiredInitializer extends Initializer {

    public AutowiredInitializer() {
        super(InitializerType.KERNEL, 20, "AutowiredInitializer",
                "Registers AutowiredBeanInterceptor for @Autowired and @Value injection");
    }

    @Override
    public void initialize(BootstrapContext context) throws ContextException {
        context.getBeanContainer().addBeanInterceptor(
                new AutowiredBeanInterceptor(context.getBeanContainer(), context.getEnvironment()));
    }
}
