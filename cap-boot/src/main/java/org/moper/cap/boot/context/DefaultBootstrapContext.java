package org.moper.cap.boot.context;

import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.bean.container.impl.DefaultBeanContainer;
import org.moper.cap.boot.annotation.InitializerExtensions;
import org.moper.cap.boot.config.DefaultConfigurationClass;
import org.moper.cap.bootstrap.Initializer;
import org.moper.cap.config.ConfigurationClass;
import org.moper.cap.context.ApplicationContext;
import org.moper.cap.context.ApplicationContextFactory;
import org.moper.cap.context.BootstrapContext;
import org.moper.cap.environment.DefaultEnvironment;
import org.moper.cap.environment.Environment;
import org.moper.cap.exception.ContextException;

import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.TreeSet;

/**
 * {@link BootstrapContext} 的默认实现。
 * 所有初始化工作在构造函数内完成。
 * {@link #build(ApplicationContextFactory)} 仅负责纯转换，不执行任何初始化逻辑。
 */
public class DefaultBootstrapContext implements BootstrapContext {

    private final BeanContainer beanContainer;
    private final Environment environment;
    private final ConfigurationClass configurationClass;

    public DefaultBootstrapContext(Class<?> primarySource, String... args) {
        // ① 初始化核心组件
        this.beanContainer = new DefaultBeanContainer();
        this.environment = new DefaultEnvironment("cap");
        this.configurationClass = new DefaultConfigurationClass(primarySource);

        // 1、 通过 SPI 发现所有 Initializer，收集到有序集合
        TreeSet<Initializer> initializers = new TreeSet<>();
        ServiceLoader<Initializer> loader = ServiceLoader.load(Initializer.class);
        for (Initializer initializer : loader) {
            initializers.add(initializer);
        }

        // 2、 通过 @InitializerExtensions 追加用户声明的 Initializer
        for(Class<? extends Initializer> cls : configurationClass.getInitializerExtensionClasses()){
            try{
                initializers.add(cls.getDeclaredConstructor().newInstance());
            }catch (Exception e){
                throw new ContextException("Failed to instantiate user-declared Initializer: " + cls.getName(), e);
            }

        }
        // 3、 按顺序执行所有 Initializer（构造函数内完成，而不是 build() 里）
        for (Initializer initializer : initializers) {
            try {
                initializer.initialize(this);
                initializer.close();
            } catch (ContextException e) {
                throw e;
            } catch (Exception e) {
                throw new ContextException("Initializer '" + initializer.name() + "' failed during initialization", e);
            }
        }
        // 构造完成后，BootstrapContext 处于完全初始化状态
    }

    @Override
    public BeanContainer getBeanContainer() { return beanContainer; }

    @Override
    public Environment getEnvironment() { return environment; }

    @Override
    public ConfigurationClass getConfigurationClass() { return configurationClass; }

    @Override
    public <T extends ApplicationContext> T build(ApplicationContextFactory<T> factory) throws ContextException {
        // 纯转换，不做任何初始化
        return factory.create(this);
    }
}
