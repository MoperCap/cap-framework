package org.moper.cap.web.annotation;

import org.moper.cap.bean.annotation.Component;
import org.moper.cap.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Marks a class as a Web MVC controller and registers it as a managed bean.
 *
 * <p>This annotation is a semantic alias of {@link Component}: it is itself
 * meta-annotated with {@code @Component}, so any class annotated with
 * {@code @Controller} will be detected and registered by the component scan.
 * The {@link #value()} attribute is bridged to {@link Component#value()} via
 * {@link AliasFor}, so an explicit bean name can be provided directly on this
 * annotation.
 *
 * <p><b>Usage examples:</b>
 * <pre>{@code
 * // Default bean name derived from class name ("userController")
 * @Controller
 * public class UserController {
 *
 *     @GetMapping("/users")
 *     public String listUsers() { ... }
 * }
 *
 * // Explicit bean name
 * @Controller("userCtrl")
 * public class UserController { ... }
 * }</pre>
 *
 * @see Component
 * @see AliasFor
 * @see RestController
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Controller {

    /**
     * The bean name for this controller, aliased to {@link Component#value()}.
     *
     * <p>Defaults to {@code ""}, which means the container will use the simple class
     * name (first letter lower-cased) as the bean name.
     *
     * @return the explicit bean name, or {@code ""} for the default
     */
    @AliasFor(annotation = Component.class, attribute = "value")
    String value() default "";
}
