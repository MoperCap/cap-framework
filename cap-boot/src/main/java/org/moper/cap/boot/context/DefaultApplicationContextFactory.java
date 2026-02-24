package org.moper.cap.boot.context;

import org.moper.cap.context.context.ApplicationContextFactory;
import org.moper.cap.context.context.BootstrapContext;
import org.moper.cap.context.exception.ContextException;

/**
 * {@link ApplicationContextFactory} 的默认实现，创建 {@link DefaultApplicationContext} 实例
 */
public class DefaultApplicationContextFactory implements ApplicationContextFactory<DefaultApplicationContext> {

    public static final DefaultApplicationContextFactory INSTANCE = new DefaultApplicationContextFactory();

    private DefaultApplicationContextFactory() {
    }

    @Override
    public DefaultApplicationContext create(BootstrapContext context) throws ContextException {
        return new DefaultApplicationContext(context);
    }
}
