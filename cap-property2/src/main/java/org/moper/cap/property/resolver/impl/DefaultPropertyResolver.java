package org.moper.cap.property.resolver.impl;

import org.moper.cap.property.exception.PropertyTypeMismatchException;
import org.moper.cap.property.resolver.PropertyConverter;
import org.moper.cap.property.resolver.PropertyResolver;
import org.moper.cap.property.resolver.converters.BasicConverters;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultPropertyResolver implements PropertyResolver {

    private final Map<ConverterKey, PropertyConverter<?, ?>> converters;

    private DefaultPropertyResolver(Map<ConverterKey, PropertyConverter<?, ?>> converters) {
        this.converters = converters;
    }

    /**
     * 类型安全地将对象value转换为目标类型targetType。
     * 找不到合适转换器或转换失败时抛出异常。
     *
     * @param value
     * @param targetType
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T resolve(Object value, Class<T> targetType) {
        if (value == null) return null;
        if (targetType.isInstance(value)) return (T) value;

        @SuppressWarnings("rawtypes")
        PropertyConverter converter = converters.get(new ConverterKey(value.getClass(), targetType));
        if (converter != null) {
            try {
                return (T) converter.convert(value);
            } catch (Exception e) {
                throw new PropertyTypeMismatchException("PropertyConverter failed to cast: " + value.getClass().getName() + " -> " + targetType.getName(), e);
            }
        }
        if (targetType.isEnum()) {
            return parseEnum(value, targetType);
        }
        throw new PropertyTypeMismatchException("PropertyConverter not found: " + value.getClass().getName() + " -> " + targetType.getName());
    }

    /**
     * 判定是否存在从sourceType到targetType的转换器。
     *
     * @param sourceType
     * @param targetType
     */
    @Override
    public boolean hasConverter(Class<?> sourceType, Class<?> targetType) {
        return converters.containsKey(new ConverterKey(sourceType, targetType));
    }

    @SuppressWarnings("unchecked")
    private <T> T parseEnum(Object value, Class<T> targetType) {
        if (value instanceof String s) {
            return (T) Enum.valueOf((Class<Enum>) targetType, s.trim());
        }
        throw new IllegalArgumentException("枚举转换仅支持字符串类型");
    }

    /**
     * 内部记录类，用于唯一标识类型转换器的源类型和目标类型组合。
     *
     * @param sourceType 转换器的输入类型
     * @param targetType 转换器的输出类型
     */
    private record ConverterKey(Class<?> sourceType, Class<?> targetType) {}

    public static class Builder{
        private final Map<ConverterKey, PropertyConverter<?, ?>> converters = new ConcurrentHashMap<>();
        private boolean enableBasicConverters = true;
        private boolean enableSPI = false;

        public Builder enableBasicConverters() {
            this.enableBasicConverters = true;
            return this;
        }

        public Builder enableBasicConverters(boolean enable) {
            this.enableBasicConverters = enable;
            return this;
        }

        public Builder enableSPI() {
            this.enableSPI = true;
            return this;
        }

        public Builder enableSPI(boolean enable) {
            this.enableSPI = enable;
            return this;
        }

        public Builder addConverter(PropertyConverter<?, ?> converter) {
            converters.put(new ConverterKey(converter.getSourceType(), converter.getTargetType()), converter);
            return this;
        }

        public DefaultPropertyResolver build() {
            if(enableSPI){
                @SuppressWarnings("rawtypes")
                ServiceLoader<PropertyConverter> loader = ServiceLoader.load(PropertyConverter.class);
                for(PropertyConverter<?, ?> converter : loader) {
                    ConverterKey key = new ConverterKey(converter.getSourceType(), converter.getTargetType());
                    converters.putIfAbsent(key, converter);
                }
            }

            if(enableBasicConverters){
                List<PropertyConverter<?, ?>> basicConverters = BasicConverters.getAllBasicConverters();
                for (PropertyConverter<?, ?> converter : basicConverters) {
                    ConverterKey key = new ConverterKey(converter.getSourceType(), converter.getTargetType());
                    converters.putIfAbsent(key, converter);
                }
            }

            return new DefaultPropertyResolver(Collections.unmodifiableMap(converters));
        }

    }
}
