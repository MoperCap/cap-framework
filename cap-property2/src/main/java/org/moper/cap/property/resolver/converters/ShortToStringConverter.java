package org.moper.cap.property.resolver.converters;

import org.moper.cap.property.resolver.PropertyConverter;

public class ShortToStringConverter implements PropertyConverter<Short, String> {
    @Override public Class<Short> getSourceType() { return Short.class; }
    @Override public Class<String> getTargetType() { return String.class; }
    @Override public String convert(Short value) { return value == null ? null : value.toString(); }
}