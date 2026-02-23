package org.moper.cap.boot.context;

import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.bean.container.impl.DefaultBeanContainer;
import org.moper.cap.bootstrap.Initializer;
import org.moper.cap.context.ApplicationContext;
import org.moper.cap.context.ApplicationContextFactory;
import org.moper.cap.context.BootstrapContext;
import org.moper.cap.environment.DefaultEnvironment;
import org.moper.cap.environment.Environment;
import org.moper.cap.exception.ContextException;

import java.util.TreeSet;

/**
 * {@link BootstrapContext} 的抽象基类 </br>
 * 持有 BeanContainer、Environment 和有序的 Initializer 列表 </br>
 * 子类通过 {@link #addInitializer(Initializer)} 注册构造机
 */
public abstract class AbstractBootstrapContext implements BootstrapContext {

    private final BeanContainer beanContainer = new DefaultBeanContainer();
    private final Environment environment = new DefaultEnvironment();
    private final TreeSet<Initializer> initializers = new TreeSet<>();
    private boolean built = false;

    @Override
    public BeanContainer getBeanContainer() {
        return beanContainer;
    }

    @Override
    public Environment getEnvironment() {
        return environment;
    }

    @Override
    public void addInitializer(Initializer initializer) {
        if (built) {
            throw new ContextException("BootstrapContext has already been built, cannot add more initializers");
        }
        initializers.add(initializer);
    }

    @Override
    public <T extends ApplicationContext> T build(ApplicationContextFactory<T> factory) throws ContextException {
        if (built) {
            throw new ContextException("BootstrapContext has already been built");
        }
        for (Initializer initializer : initializers) {
            try {
                initializer.initialize(this);
            } catch (ContextException e) {
                throw e;
            } catch (Exception e) {
                throw new ContextException("Initializer '" + initializer.name() + "' failed during initialization", e);
            }
        }
        built = true;
        return factory.create(this);
    }
}
