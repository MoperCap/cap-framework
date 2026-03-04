package org.moper.cap.core.util;

import org.moper.cap.bean.annotation.Component;
import org.moper.cap.core.exception.AnnotationConflictException;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Validates that a class does not carry more than one mutually exclusive
 * {@code @Component} semantic annotation.
 *
 * <p>Following Spring Framework convention, each class may be marked with at most one
 * "stereotype" annotation – an annotation that is itself annotated with {@link Component}
 * (or {@code Component} itself).  Typical examples are {@code @Controller},
 * {@code @RestController}, {@code @Service}, and {@code @Configuration}.
 *
 * <p>Placing two or more such annotations on the same class (e.g. {@code @Service} and
 * {@code @Controller}) is almost certainly a configuration mistake and causes this validator
 * to throw an {@link AnnotationConflictException}.
 *
 * <p><b>Detection algorithm:</b>
 * <ol>
 *   <li>Collect every annotation {@code A} on the class where either
 *       {@code A == @Component} or {@code A} is itself annotated with {@code @Component}
 *       as a direct meta-annotation.</li>
 *   <li>If more than one such annotation is found, throw {@link AnnotationConflictException}
 *       with a message listing the conflicting types and a fix suggestion.</li>
 * </ol>
 *
 * <p><b>Example – invalid:</b>
 * <pre>{@code
 * // Both @Service and @Controller are @Component aliases → conflict
 * @Service
 * @Controller
 * public class OrderHandler { ... }
 * }</pre>
 *
 * <p><b>Example – valid:</b>
 * <pre>{@code
 * @Service
 * public class OrderService { ... }
 * }</pre>
 *
 * @see AnnotationConflictException
 * @see Component
 * @see AnnotationUtils
 */
public final class AnnotationMutualExclusivity {

    private AnnotationMutualExclusivity() {
    }

    /**
     * Validates that {@code clazz} carries at most one {@code @Component} semantic annotation.
     *
     * @param clazz the class to validate
     * @throws AnnotationConflictException if two or more {@code @Component} semantic
     *                                     annotations are found on the class
     */
    public static void validate(Class<?> clazz) {
        List<Class<? extends Annotation>> componentAnnotations = new ArrayList<>();

        for (Annotation annotation : clazz.getAnnotations()) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            if (annotationType == Component.class
                    || annotationType.isAnnotationPresent(Component.class)) {
                componentAnnotations.add(annotationType);
            }
        }

        if (componentAnnotations.size() > 1) {
            String names = componentAnnotations.stream()
                    .map(t -> "@" + t.getSimpleName())
                    .collect(Collectors.joining(", "));
            throw new AnnotationConflictException(
                    "Class '" + clazz.getName()
                    + "' has multiple mutually exclusive @Component semantic annotations: "
                    + names + ". Only one @Component semantic annotation is allowed per class."
                    + " Fix: remove all but one of " + names + " from the class.",
                    componentAnnotations);
        }
    }
}
