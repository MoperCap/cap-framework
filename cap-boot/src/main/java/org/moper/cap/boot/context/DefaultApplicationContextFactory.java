package org.moper.cap.boot.context;

import org.moper.cap.context.ApplicationContextFactory;
import org.moper.cap.context.BootstrapContext;
import org.moper.cap.context.DefaultApplicationContext;
import org.moper.cap.exception.ContextException;

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
