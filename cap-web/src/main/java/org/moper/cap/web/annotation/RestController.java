package org.moper.cap.web.annotation;

import org.moper.cap.bean.annotation.Component;
import org.moper.cap.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Marks a class as a RESTful controller whose handler methods automatically serialize
 * their return values to JSON and write them to the HTTP response body.
 *
 * <p>This annotation combines the semantics of {@link Controller} and
 * {@link ResponseBody}: it is itself meta-annotated with {@link Component} (so the class
 * is registered as a bean) and implies {@code @ResponseBody} on all handler methods.
 * The {@link #value()} attribute is bridged to {@link Component#value()} via
 * {@link AliasFor}, so an explicit bean name can be provided directly on this annotation.
 *
 * <p><b>Usage examples:</b>
 * <pre>{@code
 * // Default bean name derived from class name ("userRestController")
 * @RestController
 * public class UserRestController {
 *
 *     @GetMapping("/api/users")
 *     public List<User> getUsers() { ... }
 * }
 *
 * // Explicit bean name
 * @RestController("userApi")
 * public class UserRestController { ... }
 * }</pre>
 *
 * @see Controller
 * @see ResponseBody
 * @see Component
 * @see AliasFor
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RestController {

    /**
     * The bean name for this REST controller, aliased to {@link Component#value()}.
     *
     * <p>Defaults to {@code ""}, which means the container will use the simple class
     * name (first letter lower-cased) as the bean name.
     *
     * @return the explicit bean name, or {@code ""} for the default
     */
    @AliasFor(annotation = Component.class, attribute = "value")
    String value() default "";
}
