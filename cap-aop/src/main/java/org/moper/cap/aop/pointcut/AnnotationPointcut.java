package org.moper.cap.aop.pointcut;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * A {@link Pointcut} that matches methods or classes annotated with a specific annotation.
 *
 * <p>Supports two modes:
 * <ul>
 *   <li>{@link Mode#METHOD} – matches if the method itself carries the annotation
 *       (also checks the corresponding method on {@code targetClass})</li>
 *   <li>{@link Mode#TARGET} – matches if the target class carries the annotation</li>
 * </ul>
 */
public class AnnotationPointcut implements Pointcut {

    public enum Mode { METHOD, TARGET }

    private final Mode mode;
    private final String annotationFqn;

    public AnnotationPointcut(Mode mode, String annotationFqn) {
        this.mode = mode;
        this.annotationFqn = annotationFqn;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean matches(Method method, Class<?> targetClass) {
        try {
            Class<? extends Annotation> annotationClass =
                    (Class<? extends Annotation>) Class.forName(annotationFqn);

            if (mode == Mode.METHOD) {
                if (method.isAnnotationPresent(annotationClass)) {
                    return true;
                }
                // Also look up the corresponding method on targetClass for implementations
                if (targetClass != null && targetClass != method.getDeclaringClass()) {
                    Method targetMethod = findMethod(targetClass, method.getName(), method.getParameterTypes());
                    if (targetMethod != null && targetMethod.isAnnotationPresent(annotationClass)) {
                        return true;
                    }
                }
                return false;
            } else {
                // Mode.TARGET: check the annotation on the class
                Class<?> classToCheck = targetClass != null ? targetClass : method.getDeclaringClass();
                return classToCheck.isAnnotationPresent(annotationClass);
            }
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /** Finds a method by name and parameter types, traversing the class hierarchy. */
    private static Method findMethod(Class<?> clazz, String name, Class<?>[] paramTypes) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                Method m = current.getDeclaredMethod(name, paramTypes);
                m.setAccessible(true);
                return m;
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return (mode == Mode.METHOD ? "@method(" : "@target(") + annotationFqn + ")";
    }
}
