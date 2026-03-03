package org.moper.cap.web.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 字符串到常用 Java 类型的转换工具类。
 *
 * <p>支持以下类型转换：
 * <ul>
 *   <li>{@link String}</li>
 *   <li>{@code int} / {@link Integer}</li>
 *   <li>{@code long} / {@link Long}</li>
 *   <li>{@code double} / {@link Double}</li>
 *   <li>{@code float} / {@link Float}</li>
 *   <li>{@code boolean} / {@link Boolean}</li>
 *   <li>{@link LocalDateTime}</li>
 *   <li>{@link UUID}</li>
 * </ul>
 */
public final class TypeConverter {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private TypeConverter() {
    }

    /**
     * 将字符串值转换为目标类型。
     *
     * @param value      要转换的字符串值（可为 null）
     * @param targetType 目标类型
     * @return 转换后的对象，若 value 为 null 且目标类型为原始类型则返回其默认值
     * @throws IllegalArgumentException 如果类型不受支持或转换失败
     */
    @SuppressWarnings("unchecked")
    public static <T> T convert(String value, Class<T> targetType) {
        if (targetType == null) {
            throw new IllegalArgumentException("targetType must not be null");
        }
        if (targetType == String.class) {
            return (T) value;
        }
        if (value == null || value.isEmpty()) {
            return defaultForPrimitive(targetType);
        }
        if (targetType == int.class || targetType == Integer.class) {
            return (T) Integer.valueOf(value.trim());
        }
        if (targetType == long.class || targetType == Long.class) {
            return (T) Long.valueOf(value.trim());
        }
        if (targetType == double.class || targetType == Double.class) {
            return (T) Double.valueOf(value.trim());
        }
        if (targetType == float.class || targetType == Float.class) {
            return (T) Float.valueOf(value.trim());
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            return (T) Boolean.valueOf(value.trim());
        }
        if (targetType == LocalDateTime.class) {
            return (T) LocalDateTime.parse(value.trim(), DATE_TIME_FORMATTER);
        }
        if (targetType == UUID.class) {
            return (T) UUID.fromString(value.trim());
        }
        throw new IllegalArgumentException("Unsupported target type: " + targetType.getName());
    }

    /**
     * 判断给定类型是否受支持。
     *
     * @param type 目标类型
     * @return 是否受支持
     */
    public static boolean supports(Class<?> type) {
        return type == String.class
                || type == int.class || type == Integer.class
                || type == long.class || type == Long.class
                || type == double.class || type == Double.class
                || type == float.class || type == Float.class
                || type == boolean.class || type == Boolean.class
                || type == LocalDateTime.class
                || type == UUID.class;
    }

    @SuppressWarnings("unchecked")
    private static <T> T defaultForPrimitive(Class<T> type) {
        if (type == int.class) return (T) Integer.valueOf(0);
        if (type == long.class) return (T) Long.valueOf(0L);
        if (type == double.class) return (T) Double.valueOf(0.0);
        if (type == float.class) return (T) Float.valueOf(0.0f);
        if (type == boolean.class) return (T) Boolean.FALSE;
        return null;
    }
}
