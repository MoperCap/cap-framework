package org.moper.cap.property.resolver;

import org.moper.cap.property.exception.PropertyTypeMismatchException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 属性类型安全解析与转换工具，支持用户注册自定义类型转换器。
 */
public final class PropertyResolver {

    // 默认类型转换器（只读，不可修改）
    private static final Map<Class<?>, Function<Object, ?>> DEFAULT_CONVERTERS = Map.of(
            String.class, PropertyResolver::toStringValue,
            Integer.class, PropertyResolver::toIntegerValue,
            Long.class, PropertyResolver::toLongValue,
            Double.class, PropertyResolver::toDoubleValue,
            Boolean.class, PropertyResolver::toBooleanValue,
            Float.class, PropertyResolver::toFloatValue,
            Short.class, PropertyResolver::toShortValue,
            Byte.class, PropertyResolver::toByteValue
    );

    // 用户自定义的类型转换器（线程安全，优先使用）
    private static final Map<Class<?>, Function<Object, ?>> customConverters = new ConcurrentHashMap<>();

    private PropertyResolver() {}

    /**
     * 用户注册自定义类型转换器。如果类型已存在，将覆盖。
     * @param targetType 目标类型
     * @param converter 转换函数
     */
    public static <T> void registerConverter(Class<T> targetType, Function<Object, T> converter) {
        if (targetType == null || converter == null) {
            throw new IllegalArgumentException("targetType and converter must not be null");
        }
        customConverters.put(targetType, (Function<Object, ?>) converter);
    }

    /**
     * 类型安全转换方法。优先用用户注册转换器，其次用默认转换器。
     * 支持枚举类型。
     */
    @SuppressWarnings("unchecked")
    public static <T> T resolve(Object value, Class<T> targetType) {
        if (value == null) return null;

        if (targetType.isInstance(value)) {
            return (T) value;
        }

        // 优先用用户自定义转换器
        Function<Object, ?> converter = customConverters.get(targetType);
        if (converter != null) {
            try {
                return (T) converter.apply(value);
            } catch (Exception e) {
                throw new PropertyTypeMismatchException("Custom converter failed to convert value '" + value + "' to " + targetType.getName(), e);
            }
        }

        // 枚举类型特殊处理
        if (targetType.isEnum()) {
            if (value instanceof String str) {
                try {
                    return (T) Enum.valueOf((Class<Enum>) targetType, str);
                } catch (Exception e) {
                    throw new PropertyTypeMismatchException("Cannot convert value '" + str + "' to enum " + targetType.getName(), e);
                }
            }
            throw new PropertyTypeMismatchException("Cannot convert non-string value to enum " + targetType.getName());
        }

        // 用默认转换器
        converter = DEFAULT_CONVERTERS.get(targetType);
        if (converter != null) {
            try {
                return (T) converter.apply(value);
            } catch (Exception e) {
                throw new PropertyTypeMismatchException("Cannot convert value '" + value + "' to " + targetType.getName(), e);
            }
        }

        throw new PropertyTypeMismatchException("Unsupported property target type: " + targetType.getName());
    }

    // --- 内部默认转换实现 ---
    private static String toStringValue(Object v) { return Objects.toString(v, null); }
    private static Integer toIntegerValue(Object v) {
        if (v instanceof Number n) return n.intValue();
        if (v instanceof String s) return Integer.parseInt(s.trim());
        throw new ClassCastException();
    }
    private static Long toLongValue(Object v) {
        if (v instanceof Number n) return n.longValue();
        if (v instanceof String s) return Long.parseLong(s.trim());
        throw new ClassCastException();
    }
    private static Double toDoubleValue(Object v) {
        if (v instanceof Number n) return n.doubleValue();
        if (v instanceof String s) return Double.parseDouble(s.trim());
        throw new ClassCastException();
    }
    private static Boolean toBooleanValue(Object v) {
        if (v instanceof Boolean b) return b;
        if (v instanceof String s) return Boolean.parseBoolean(s.trim());
        throw new ClassCastException();
    }
    private static Float toFloatValue(Object v) {
        if (v instanceof Number n) return n.floatValue();
        if (v instanceof String s) return Float.parseFloat(s.trim());
        throw new ClassCastException();
    }
    private static Short toShortValue(Object v) {
        if (v instanceof Number n) return n.shortValue();
        if (v instanceof String s) return Short.parseShort(s.trim());
        throw new ClassCastException();
    }
    private static Byte toByteValue(Object v) {
        if (v instanceof Number n) return n.byteValue();
        if (v instanceof String s) return Byte.parseByte(s.trim());
        throw new ClassCastException();
    }
}