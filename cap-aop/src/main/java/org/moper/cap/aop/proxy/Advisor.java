package org.moper.cap.aop.proxy;

import lombok.Getter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class Advisor {
    public enum Type { BEFORE, AFTER, AROUND }
    @Getter
    private final Type type;
    private final String pointcut;
    @Getter
    private final Object aspectInstance;
    @Getter
    private final Method adviceMethod;

    public Advisor(Type type, String pointcut, Object aspectInstance, Method adviceMethod) {
        this.type = type;
        this.pointcut = pointcut;
        this.aspectInstance = aspectInstance;
        this.adviceMethod = adviceMethod;
    }

    /**
     * Matches a method using the pointcut expression.
     *
     * <p>Supports two matching modes:
     * <ul>
     *   <li><b>Exact signature:</b> {@code "com.example.Foo.bar"}</li>
     *   <li><b>Annotation-based:</b> {@code "@method(annotationFqn)"} or {@code "@target(annotationFqn)"},
     *       optionally combined with {@code " || "}</li>
     * </ul>
     *
     * @param m the method to match (may be an interface or proxy method)
     */
    public boolean matches(Method m) {
        return matches(m, null);
    }

    /**
     * Matches a method using the pointcut expression, with awareness of the actual target class.
     *
     * <p>When {@code targetClass} is provided, annotation-based patterns also look up
     * the corresponding method on {@code targetClass} to find annotations that may be
     * absent on the proxy or interface method.
     *
     * @param m           the method to match (may be an interface or proxy method)
     * @param targetClass the actual implementation class (may be {@code null})
     */
    public boolean matches(Method m, Class<?> targetClass) {
        if (isAnnotationBased(pointcut)) {
            return matchesAnnotationPointcut(m, targetClass, pointcut);
        }
        String sig = m.getDeclaringClass().getName() + "." + m.getName();
        return sig.equals(pointcut);
    }

    private static boolean isAnnotationBased(String expr) {
        return expr.contains("@method(") || expr.contains("@target(");
    }

    private static boolean matchesAnnotationPointcut(Method m, Class<?> targetClass, String expr) {
        if (expr.contains(" || ")) {
            for (String part : expr.split("\\|\\|")) {
                if (matchesSingleAnnotationPointcut(m, targetClass, part.trim())) {
                    return true;
                }
            }
            return false;
        }
        return matchesSingleAnnotationPointcut(m, targetClass, expr);
    }

    @SuppressWarnings("unchecked")
    private static boolean matchesSingleAnnotationPointcut(Method m, Class<?> targetClass, String expr) {
        if (expr.startsWith("@method(") && expr.endsWith(")")) {
            String annotationFqn = expr.substring(8, expr.length() - 1).trim();
            try {
                Class<? extends Annotation> annotationClass =
                        (Class<? extends Annotation>) Class.forName(annotationFqn);
                // Check the method itself first (works for implementation-class methods)
                if (m.isAnnotationPresent(annotationClass)) {
                    return true;
                }
                // Also look up the corresponding method on the target class
                if (targetClass != null && targetClass != m.getDeclaringClass()) {
                    Method targetMethod = findMethod(targetClass, m.getName(), m.getParameterTypes());
                    if (targetMethod != null && targetMethod.isAnnotationPresent(annotationClass)) {
                        return true;
                    }
                }
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
        if (expr.startsWith("@target(") && expr.endsWith(")")) {
            String annotationFqn = expr.substring(8, expr.length() - 1).trim();
            try {
                Class<? extends Annotation> annotationClass =
                        (Class<? extends Annotation>) Class.forName(annotationFqn);
                Class<?> classToCheck = targetClass != null ? targetClass : m.getDeclaringClass();
                return classToCheck.isAnnotationPresent(annotationClass);
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
        return false;
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
}