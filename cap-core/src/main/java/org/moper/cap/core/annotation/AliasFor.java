package org.moper.cap.core.annotation;

import java.lang.annotation.*;

/**
 * Declares that an annotation attribute is an alias for an attribute in another annotation
 * (or for another attribute within the same annotation).
 *
 * <h2>Meta-annotation alias (cross-annotation)</h2>
 * <p>When {@link #annotation()} points to a <em>different</em> annotation type, the attribute
 * decorated with {@code @AliasFor} is an alias for the specified attribute of the target
 * annotation.  The target annotation must be present as a meta-annotation on the enclosing
 * annotation.
 *
 * <pre>{@code
 * // @Controller.value() is an alias for @Component.value()
 * @Component
 * public @interface Controller {
 *
 *     @AliasFor(annotation = Component.class, attribute = "value")
 *     String value() default "";
 * }
 *
 * // @Controller("myCtrl") effectively sets @Component.value = "myCtrl"
 * @Controller("myCtrl")
 * public class MyController { ... }
 * }</pre>
 *
 * <h2>Same-annotation alias (self-alias)</h2>
 * <p>When {@link #annotation()} is omitted (or set to {@link Annotation}{@code .class}),
 * the attribute is an alias for another attribute within the <em>same</em> annotation.
 * Both attributes must mutually reference each other and have identical default values.
 *
 * <pre>{@code
 * public @interface Bean {
 *
 *     @AliasFor("name")
 *     String[] value() default {};
 *
 *     @AliasFor("value")
 *     String[] name() default {};
 * }
 * }</pre>
 *
 * <p>This annotation does not enforce its semantics at the Java compiler level; enforcement
 * is delegated to {@link org.moper.cap.core.util.AnnotationUtils}.
 *
 * @see org.moper.cap.core.util.AnnotationUtils
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AliasFor {

    /**
     * The name of the attribute that this attribute is an alias for.
     *
     * <p>Defaults to {@code ""}, which means the annotated attribute itself serves as the
     * canonical name.  In a same-annotation alias pair each side should reference the other.
     *
     * @return the aliased attribute name
     */
    String attribute() default "";

    /**
     * The annotation type that declares the aliased attribute.
     *
     * <p>Defaults to {@link Annotation}{@code .class}, which is used as a sentinel to
     * indicate a <em>same-annotation</em> alias (i.e. the aliased attribute lives in the
     * same annotation as the decorated attribute).
     *
     * @return the target annotation type, or {@link Annotation}{@code .class} for self-alias
     */
    Class<? extends Annotation> annotation() default Annotation.class;
}
