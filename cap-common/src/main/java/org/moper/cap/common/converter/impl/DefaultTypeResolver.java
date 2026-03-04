package org.moper.cap.common.converter.impl;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.converter.TypeConversionException;
import org.moper.cap.common.converter.TypeConverter;
import org.moper.cap.common.converter.TypeResolver;

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
            ConverterKey key = new ConverterKey(c.getSourceType(), c.getTargetType());
            TypeConverter<?, ?> prev = map.get(key);
            if(prev == null){
                log.debug("注册类型转换器 [{} -> {}]: {} (priority={})", key.source, key.target, c.getClass().getName(), c.getOrder());
                map.put(key, c);
            }else if(c.getOrder() < prev.getOrder()){
                log.debug("覆盖类型转换器 [{} -> {}]: {} <== {} ({} < {})",
                        key.source, key.target, prev.getClass().getName(), c.getClass().getName(), prev.getOrder(), c.getOrder());
                map.put(key, c);
            }else {
                log.debug("忽略较低优先级的类型转换器 [{} -> {}]: {} (priority={}), 被 {} (priority={}) 覆盖",
                        key.source, key.target, c.getClass().getName(), c.getOrder(), prev.getClass().getName(), prev.getOrder());
            }
        }
        this.converters = Collections.unmodifiableMap(map);
        log.info("DefaultPropertyResolver 共注册 {} 种类型转换器", converters.size());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T resolve(Object value, Class<T> targetType) {
        if(targetType == null) {
            throw new IllegalArgumentException("Target type cannot be null");
        }

        if (value == null) return null;
        if (targetType.isInstance(value)) return (T) value;
        @SuppressWarnings("rawtypes")
        TypeConverter converter = converters.get(new ConverterKey(value.getClass(), targetType));
        if (converter != null) {
            try {
                return (T) converter.convert(value);
            } catch (Exception e) {
                log.warn("类型转换失败 [{} -> {}]，异常：{}", value.getClass().getName(), targetType.getName(), e.getMessage(), e);
                throw new TypeConversionException("类型转换失败: " + value.getClass().getName() + " -> " + targetType.getName(), e);
            }
        }
        if (targetType.isEnum()) {
            return parseEnum(value, targetType);
        }
        log.warn("找不到类型转换器 [{} -> {}]，值: {}", value.getClass().getName(), targetType.getName(), value);
        throw new TypeConversionException("未找到类型转换器: " + value.getClass().getName() + " -> " + targetType.getName());
    }

    @Override
    public boolean hasConverter(Class<?> sourceType, Class<?> targetType) {
        return converters.containsKey(new ConverterKey(sourceType, targetType));
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