package org.moper.cap.core.util;

import org.moper.cap.bean.annotation.Component;
import org.moper.cap.core.annotation.AliasFor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility methods for working with annotations, with support for {@link AliasFor}-based
 * attribute aliasing.
 *
 * <h2>Attribute resolution</h2>
 * <p>The core operation is {@link #getAnnotationAttribute(Annotation, String)}, which
 * retrieves the value of a named attribute from an annotation while following any
 * {@link AliasFor} self-alias chains defined within the same annotation type.
 *
 * <p>Circular alias references (e.g. {@code a → b → a}) are detected and reported with a
 * clear {@link IllegalStateException}.
 *
 * <h2>Bean-name resolution for {@code @Component} aliases</h2>
 * <p>{@link #resolveComponentBeanName(Class)} inspects all annotations on a class and
 * returns the effective {@code @Component.value()} bean name, following any
 * {@link AliasFor} chains that bridge a meta-annotation attribute back to
 * {@code @Component.value()}.
 *
 * <p><b>Examples:</b>
 * <pre>{@code
 * // Direct @Component annotation
 * @Component("myService")
 * class MyService {}
 * AnnotationUtils.resolveComponentBeanName(MyService.class); // "myService"
 *
 * // @Controller with @AliasFor bridging to @Component.value
 * @Controller("myCtrl")
 * class MyController {}
 * AnnotationUtils.resolveComponentBeanName(MyController.class); // "myCtrl"
 * }</pre>
 *
 * @see AliasFor
 */
public final class AnnotationUtils {

    private AnnotationUtils() {
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Returns the value of the named attribute from {@code annotation}, resolving any
     * same-annotation {@link AliasFor} aliases defined in the annotation type.
     *
     * <p>Only <em>self-alias</em> chains (where {@link AliasFor#annotation()} points to the
     * same annotation type) are followed.  Meta-annotation aliases are <strong>not</strong>
     * followed here; use {@link #resolveComponentBeanName(Class)} for cross-annotation
     * resolution.
     *
     * @param annotation    the annotation instance to inspect
     * @param attributeName the attribute name to retrieve
     * @return the resolved attribute value (never {@code null})
     * @throws IllegalArgumentException if the attribute does not exist on the annotation
     * @throws IllegalStateException    if a circular alias chain is detected
     */
    public static Object getAnnotationAttribute(Annotation annotation, String attributeName) {
        Set<String> visited = new HashSet<>();
        return resolveAttribute(annotation, attributeName, visited);
    }

    /**
     * Convenience wrapper around {@link #getAnnotationAttribute(Annotation, String)} that
     * casts the result to {@link String}.
     *
     * @param annotation    the annotation instance
     * @param attributeName the attribute name
     * @return the attribute value as a {@code String}, or {@code ""} if not found / not String
     */
    public static String getStringAttribute(Annotation annotation, String attributeName) {
        try {
            Object value = getAnnotationAttribute(annotation, attributeName);
            return value instanceof String s ? s : "";
        } catch (IllegalArgumentException e) {
            return "";
        }
    }

    /**
     * Convenience wrapper that casts the result to {@code String[]}.
     *
     * @param annotation    the annotation instance
     * @param attributeName the attribute name
     * @return the attribute value as a {@code String[]}, or an empty array if not applicable
     */
    public static String[] getStringArrayAttribute(Annotation annotation, String attributeName) {
        try {
            Object value = getAnnotationAttribute(annotation, attributeName);
            return value instanceof String[] arr ? arr : new String[0];
        } catch (IllegalArgumentException e) {
            return new String[0];
        }
    }

    /**
     * Convenience wrapper that casts the result to {@code boolean}.
     *
     * @param annotation    the annotation instance
     * @param attributeName the attribute name
     * @param defaultValue  value to return when the attribute is absent or not a boolean
     * @return the attribute value as a {@code boolean}
     */
    public static boolean getBooleanAttribute(Annotation annotation, String attributeName,
                                              boolean defaultValue) {
        try {
            Object value = getAnnotationAttribute(annotation, attributeName);
            return value instanceof Boolean b ? b : defaultValue;
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    /**
     * Resolves the effective {@code @Component.value()} bean name for the given class by
     * inspecting its annotations and following {@link AliasFor} meta-annotation bridges.
     *
     * <p>Resolution order:
     * <ol>
     *   <li>A direct {@code @Component} annotation – returns {@code component.value()}.</li>
     *   <li>Any annotation that is itself meta-annotated with {@code @Component} and
     *       declares an attribute carrying
     *       {@code @AliasFor(annotation = Component.class, attribute = "value")} –
     *       returns the value of that attribute.</li>
     *   <li>No match – returns {@code ""}.</li>
     * </ol>
     *
     * @param clazz the class to inspect
     * @return the bean name declared via an {@code @Component} alias, or {@code ""}
     */
    public static String resolveComponentBeanName(Class<?> clazz) {
        for (Annotation annotation : clazz.getAnnotations()) {
            Class<? extends Annotation> annotationType = annotation.annotationType();

            // Direct @Component annotation
            if (annotationType == Component.class) {
                Component component = (Component) annotation;
                return component.value();
            }

            // Annotation meta-annotated with @Component – look for @AliasFor bridge
            if (annotationType.isAnnotationPresent(Component.class)) {
                String aliasedValue = findAliasedComponentValue(annotation, annotationType);
                if (aliasedValue != null) {
                    return aliasedValue;
                }
            }
        }
        return "";
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Recursively resolves an attribute following same-annotation {@link AliasFor} chains.
     */
    private static Object resolveAttribute(Annotation annotation, String attributeName,
                                           Set<String> visited) {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        String key = annotationType.getName() + "#" + attributeName;

        if (!visited.add(key)) {
            throw new IllegalStateException(
                    "Circular @AliasFor reference detected for attribute '"
                    + attributeName + "' in annotation @" + annotationType.getSimpleName());
        }

        Method method = findMethod(annotationType, attributeName);
        if (method == null) {
            throw new IllegalArgumentException(
                    "Annotation @" + annotationType.getSimpleName()
                    + " does not declare an attribute named '" + attributeName + "'");
        }

        // Check whether this attribute has a same-annotation @AliasFor
        AliasFor aliasFor = method.getAnnotation(AliasFor.class);
        if (aliasFor != null) {
            Class<? extends Annotation> aliasAnnotation = aliasFor.annotation();
            // Same-annotation alias: annotation() is Annotation.class (sentinel) or equals annotationType
            boolean isSelfAlias = aliasAnnotation == Annotation.class
                    || aliasAnnotation == annotationType;
            if (isSelfAlias) {
                String aliasAttribute = aliasFor.attribute();
                if (!aliasAttribute.isBlank() && !aliasAttribute.equals(attributeName)) {
                    // Follow the alias
                    Object aliasValue = resolveAttribute(annotation, aliasAttribute, visited);
                    // Return alias value if it is non-default (not empty array / blank string)
                    if (!isDefaultValue(aliasValue)) {
                        return aliasValue;
                    }
                }
            }
        }

        // Return the raw attribute value
        try {
            return method.invoke(annotation);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Failed to read attribute '" + attributeName + "' from @"
                    + annotationType.getSimpleName(), e);
        }
    }

    /**
     * Scans the methods of {@code annotationType} for one with
     * {@code @AliasFor(annotation = Component.class, attribute = "value")} and, if found,
     * returns the value of that method on the given {@code annotation} instance.
     *
     * @return the aliased value, or {@code null} if no suitable method is found
     */
    private static String findAliasedComponentValue(Annotation annotation,
                                                    Class<? extends Annotation> annotationType) {
        for (Method method : annotationType.getDeclaredMethods()) {
            AliasFor aliasFor = method.getAnnotation(AliasFor.class);
            if (aliasFor == null) {
                continue;
            }
            if (aliasFor.annotation() != Component.class) {
                continue;
            }
            String targetAttribute = aliasFor.attribute();
            if (!"value".equals(targetAttribute)) {
                continue;
            }
            try {
                Object value = method.invoke(annotation);
                if (value instanceof String s) {
                    return s;
                }
            } catch (ReflectiveOperationException ignored) {
                // skip this method
            }
        }
        return null;
    }

    private static Method findMethod(Class<? extends Annotation> annotationType, String name) {
        try {
            return annotationType.getDeclaredMethod(name);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * Returns {@code true} when the value is considered "empty" / default:
     * a blank {@code String}, an empty {@code String[]}, or a {@code null}.
     */
    private static boolean isDefaultValue(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String s) {
            return s.isBlank();
        }
        if (value instanceof String[] arr) {
            return arr.length == 0;
        }
        return false;
    }
}
