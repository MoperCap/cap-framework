package org.moper.cap.aop.annotation;

import java.lang.annotation.*;

/**
 * Marks a method as an <em>after-throwing</em> advice: it is invoked when a matched
 * join point exits by throwing an exception.
 *
 * <p>The advice method may declare the following parameters (in any order):
 * <ul>
 *   <li>A {@link org.moper.cap.aop.model.JoinPoint} – receives the join point context.</li>
 *   <li>A {@link Throwable} subtype – receives the thrown exception.
 *       Only advisors whose exception type is assignable from the actual exception are invoked.</li>
 * </ul>
 *
 * <p>Example:
 * <pre>{@code
 * @AfterThrowing(pointcut = "@method(org.example.MyAnnotation)", throwing = "ex")
 * public void onError(JoinPoint jp, RuntimeException ex) { ... }
 * }</pre>
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AfterThrowing {

    /**
     * Alias for {@link #pointcut}.
     */
    String value() default "";

    /**
     * The pointcut expression.
     */
    String pointcut() default "";

    /**
     * The name of the parameter in the advice method that receives the thrown exception.
     * This is informational; the framework matches by parameter type.
     */
    String throwing() default "";
}
