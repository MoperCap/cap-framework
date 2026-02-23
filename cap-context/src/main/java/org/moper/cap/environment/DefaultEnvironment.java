package org.moper.cap.environment;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Environment} 的默认实现，内部持有多个 {@link PropertySource}，按优先级查找属性
 */
public class DefaultEnvironment implements Environment {

    private final List<PropertySource> propertySources = new ArrayList<>();

    @Override
    public @Nullable String getProperty(String key) {
        Object value = getRawProperty(key);
        return value == null ? null : String.valueOf(value);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value == null ? defaultValue : value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> @Nullable T getProperty(String key, Class<T> targetType) {
        Object value = getRawProperty(key);
        if (value == null) return null;
        if (targetType.isInstance(value)) return (T) value;
        return convertValue(String.valueOf(value), targetType);
    }

    @Override
    public boolean containsProperty(String key) {
        for (PropertySource source : propertySources) {
            if (source.containsProperty(key)) return true;
        }
        return false;
    }

    @Override
    public void addPropertySource(PropertySource propertySource) {
        propertySources.add(propertySource);
        propertySources.sort((a, b) -> Integer.compare(a.getOrder(), b.getOrder()));
    }

    @Nullable
    private Object getRawProperty(String key) {
        for (PropertySource source : propertySources) {
            if (source.containsProperty(key)) {
                return source.getProperty(key);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T convertValue(String value, Class<T> targetType) {
        if (targetType == String.class) return (T) value;
        if (targetType == Integer.class || targetType == int.class) return (T) Integer.valueOf(value);
        if (targetType == Long.class || targetType == long.class) return (T) Long.valueOf(value);
        if (targetType == Double.class || targetType == double.class) return (T) Double.valueOf(value);
        if (targetType == Float.class || targetType == float.class) return (T) Float.valueOf(value);
        if (targetType == Boolean.class || targetType == boolean.class) return (T) Boolean.valueOf(value);
        if (targetType == Short.class || targetType == short.class) return (T) Short.valueOf(value);
        if (targetType == Byte.class || targetType == byte.class) return (T) Byte.valueOf(value);
        throw new IllegalArgumentException("Cannot convert value '" + value + "' to type " + targetType.getName());
    }
}
