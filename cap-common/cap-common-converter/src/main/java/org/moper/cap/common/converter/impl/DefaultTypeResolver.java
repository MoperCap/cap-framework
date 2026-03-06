package org.moper.cap.common.converter.impl;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.converter.TypeConversionException;
import org.moper.cap.common.converter.TypeConverter;
import org.moper.cap.common.converter.TypeResolver;
import org.moper.cap.common.converter.TypeResolverFactory;
import org.moper.cap.common.priority.PriorityUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DefaultTypeResolver implements TypeResolver {
    private final Map<ConverterKey, TypeConverter<?, ?>> converters;

    public DefaultTypeResolver() {
        Map<ConverterKey, TypeConverter<?, ?>> map = new ConcurrentHashMap<>();

        // ServiceLoader发现所有cap-property默认+用户自定义
        @SuppressWarnings("rawtypes")
        ServiceLoader<TypeConverter> loader = ServiceLoader.load(TypeConverter.class);
        for (TypeConverter<?, ?> c : loader) {
            int cPriority = PriorityUtils.getPriority(c.getClass());
            ConverterKey key = new ConverterKey(c.getSourceType(), c.getTargetType());
            TypeConverter<?, ?> prev = map.get(key);
            if (prev == null) {
                log.debug("注册类型转换器 [{} -> {}]: {} (priority={})", key.source, key.target, c.getClass().getName(), cPriority);
                map.put(key, c);
            } else {
                int prevPriority = PriorityUtils.getPriority(prev.getClass());
                if (cPriority < prevPriority) {
                    log.debug("覆盖类型转换器 [{} -> {}]: {} <== {} ({} < {})",
                            key.source, key.target, prev.getClass().getName(), c.getClass().getName(), prevPriority, cPriority);
                    map.put(key, c);
                } else {
                    log.debug("忽略较低优先级的类型转换器 [{} -> {}]: {} (priority={}), 被 {} (priority={}) 覆盖",
                            key.source, key.target, c.getClass().getName(), cPriority, prev.getClass().getName(), prevPriority);
                }
            }
        }
        this.converters = Collections.unmodifiableMap(map);
        log.info("DefaultTypeResolver 共注册 {} 种类型转换器", converters.size());
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T resolve(Object value, Class<T> targetType) {
        if(targetType == null) {
            throw new IllegalArgumentException("Target type cannot be null");
        }

        if (value == null) return null;

        // 源类型等价于目标类型（含基本类型与包装类型互判），直接返回
        if (TypeResolverFactory.isEquivalent(value.getClass(), targetType)) {
            return (T) value;
        }

        Class<?> sourceType = value.getClass();

        // 策略1：精确匹配 (sourceType -> targetType)
        TypeConverter<?, ?> converter = converters.get(new ConverterKey(sourceType, targetType));
        if (converter != null) {
            return invokeConverter(converter, value, sourceType, targetType);
        }

        // 策略2：目标是基本类型，尝试其包装类型的转换器
        Class<?> wrapperTarget = targetType.isPrimitive() ? TypeResolverFactory.getWrapperType(targetType) : null;
        if (wrapperTarget != null) {
            converter = converters.get(new ConverterKey(sourceType, wrapperTarget));
            if (converter != null) {
                // 包装类型结果会被自动拆箱
                return (T) invokeConverter(converter, value, sourceType, wrapperTarget);
            }
        }

        // 策略3：源是基本类型，尝试其包装类型的转换器
        // （在 Java 运行时值已自动装箱，此分支通常不会触发，但保留以防反射等特殊场景）
        Class<?> wrapperSource = sourceType.isPrimitive() ? TypeResolverFactory.getWrapperType(sourceType) : null;
        if (wrapperSource != null) {
            converter = converters.get(new ConverterKey(wrapperSource, targetType));
            if (converter != null) {
                return invokeConverter(converter, value, wrapperSource, targetType);
            }
        }

        // 策略4：两者都转为包装类型后再查找（仅在上两步均未成功时执行）
        if (wrapperSource != null || wrapperTarget != null) {
            Class<?> effectiveSource = wrapperSource != null ? wrapperSource : sourceType;
            Class<?> effectiveTarget = wrapperTarget != null ? wrapperTarget : targetType;
            converter = converters.get(new ConverterKey(effectiveSource, effectiveTarget));
            if (converter != null) {
                return (T) invokeConverter(converter, value, effectiveSource, effectiveTarget);
            }
        }

        if (targetType.isEnum()) {
            return parseEnum(value, targetType);
        }
        log.warn("找不到类型转换器 [{} -> {}]，值: {}", sourceType.getName(), targetType.getName(), value);
        throw new TypeConversionException("未找到类型转换器: " + sourceType.getName() + " -> " + targetType.getName());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> T invokeConverter(TypeConverter<?, ?> converter, Object value, Class<?> sourceType, Class<?> targetType) {
        try {
            return (T) ((TypeConverter) converter).convert(value);
        } catch (Exception e) {
            log.warn("类型转换失败 [{} -> {}]，异常：{}", sourceType.getName(), targetType.getName(), e.getMessage(), e);
            throw new TypeConversionException("类型转换失败: " + sourceType.getName() + " -> " + targetType.getName(), e);
        }
    }

    @Override
    public boolean hasConverter(Class<?> sourceType, Class<?> targetType) {
        if (converters.containsKey(new ConverterKey(sourceType, targetType))) {
            return true;
        }
        // 检查基本类型与包装类型的等价映射
        Class<?> wrapperSource = TypeResolverFactory.getWrapperType(sourceType);
        Class<?> wrapperTarget = TypeResolverFactory.getWrapperType(targetType);
        return converters.containsKey(new ConverterKey(wrapperSource, wrapperTarget));
    }

    @SuppressWarnings("unchecked")
    private <T> T parseEnum(Object value, Class<T> targetType) {
        if (value instanceof String s) {
            return (T) Enum.valueOf((Class<Enum>) targetType, s.trim());
        }
        log.warn("枚举转换目前仅支持字符串类型，当前值类型: {}", value.getClass().getName());
        throw new IllegalArgumentException("枚举转换仅支持字符串类型");
    }

    private record ConverterKey(Class<?> source, Class<?> target) {}
}