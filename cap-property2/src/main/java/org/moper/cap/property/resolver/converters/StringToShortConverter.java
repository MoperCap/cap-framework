package org.moper.cap.property.resolver.converters;

import org.moper.cap.property.resolver.PropertyConverter;

public class StringToShortConverter implements PropertyConverter<String, Short> {
    @Override public Class<String> getSourceType() { return String.class; }
    @Override public Class<Short> getTargetType() { return Short.class; }
    @Override public Short convert(String value) { return value == null ? null : Short.parseShort(value.trim()); }
}