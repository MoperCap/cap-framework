package org.moper.cap.property.resolver.converters;

import org.moper.cap.property.resolver.PropertyConverter;

public class StringToLongConverter implements PropertyConverter<String, Long> {
    @Override public Class<String> getSourceType() { return String.class; }
    @Override public Class<Long> getTargetType() { return Long.class; }
    @Override public Long convert(String value) { return value == null ? null : Long.parseLong(value.trim()); }
}