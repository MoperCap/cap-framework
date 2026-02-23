package org.moper.cap.boot.context;

import org.moper.cap.boot.annotation.InitializerExtensions;
import org.moper.cap.bootstrap.Initializer;
import org.moper.cap.environment.MapPropertySource;
import org.moper.cap.exception.ContextException;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * {@link AbstractBootstrapContext} 的默认实现 </br>
 * 通过 Java SPI 自动发现并注册 Initializer </br>
 * 通过 {@link InitializerExtensions} 注解注册用户声明的 Initializer
 */
public class DefaultBootstrapContext extends AbstractBootstrapContext {

    public DefaultBootstrapContext(Class<?> primarySource, String... args) {
        // Write bootstrap properties to environment
        Map<String, Object> bootstrapProps = new HashMap<>();
        bootstrapProps.put("cap.primary-source", primarySource.getName());
        if (args != null && args.length > 0) {
            bootstrapProps.put("cap.args", String.join(",", args));
        }
        getEnvironment().addPropertySource(new MapPropertySource("bootstrap", bootstrapProps));

        // SPI: discover and register all Initializer implementations
        ServiceLoader<Initializer> loader = ServiceLoader.load(Initializer.class);
        for (Initializer initializer : loader) {
            addInitializer(initializer);
        }

        // @InitializerExtensions: register user-declared Initializer extensions
        InitializerExtensions extensions = primarySource.getAnnotation(InitializerExtensions.class);
        if (extensions != null) {
            for (Class<? extends Initializer> cls : extensions.value()) {
                try {
                    addInitializer(cls.getDeclaredConstructor().newInstance());
                } catch (Exception e) {
                    throw new ContextException(
                            "Failed to instantiate user-declared Initializer: " + cls.getName(), e);
                }
            }
        }
    }
}
