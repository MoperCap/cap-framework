package org.moper.cap.core.exception;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Exception thrown when a class carries multiple mutually exclusive annotations.
 *
 * <p>Following Spring Framework strategy, each class may carry only one
 * {@code @Component} semantic annotation (e.g. {@code @Controller},
 * {@code @Service}, {@code @Configuration}).  Placing two or more such
 * annotations on the same class is considered a configuration error and
 * results in this exception being thrown during bootstrap.
 *
 * <p><b>Fix suggestion:</b> Remove all but one of the conflicting annotations
 * from the reported class.
 *
 * <p><b>Example:</b>
 * <pre>{@code
 * // WRONG – @Service and @Controller are mutually exclusive @Component aliases
 * @Service
 * @Controller
 * public class OrderHandler { ... }
 *
 * // OK – only one semantic @Component annotation per class
 * @Service
 * public class OrderService { ... }
 * }</pre>
 */
public class AnnotationConflictException extends RuntimeException {

    private final List<Class<? extends Annotation>> conflictingAnnotations;

    /**
     * Creates a new exception with the given message and list of conflicting annotation types.
     *
     * @param message                the detail message describing the conflict
     * @param conflictingAnnotations the list of annotation types that conflict with each other
     */
    public AnnotationConflictException(String message,
                                       List<Class<? extends Annotation>> conflictingAnnotations) {
        super(message);
        this.conflictingAnnotations = List.copyOf(conflictingAnnotations);
    }

    /**
     * Returns the annotation types that caused the conflict.
     *
     * @return an unmodifiable list of conflicting annotation types
     */
    public List<Class<? extends Annotation>> getConflictingAnnotations() {
        return conflictingAnnotations;
    }
}
