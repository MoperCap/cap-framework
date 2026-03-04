package org.moper.cap.core.annotation;

import org.moper.cap.bean.annotation.Component;

import java.lang.annotation.*;

/**
 * Generic marker for a configuration class that contains Bean definitions or framework
 * configuration.
 *
 * <p>This annotation is a meta-annotation alias of {@link Component}: it is itself
 * annotated with {@code @Component}, so classes marked with {@code @Configuration} are
 * automatically registered as managed beans by the container.  Different container
 * implementations may scan and process this annotation independently.
 *
 * <p><b>Usage example:</b>
 * <pre>{@code
 * @Configuration
 * public class AppConfig {
 *
 *     @Bean
 *     public DataSource dataSource() { ... }
 * }
 *
 * // Explicit bean name
 * @Configuration("appConfig")
 * public class AppConfig { ... }
 * }</pre>
 *
 * @see Component
 * @see AliasFor
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Configuration {

    /**
     * The bean name for this configuration class.
     *
     * <p>Defaults to {@code ""}, which means the container will derive the bean name from
     * the simple class name (first letter lower-cased).
     *
     * @return the explicit bean name, or {@code ""} to use the default
     */
    String value() default "";
}
