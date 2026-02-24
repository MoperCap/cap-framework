package org.moper.cap.property.resolver.converters;

import org.moper.cap.property.resolver.PropertyConverter;

public class LongToStringConverter implements PropertyConverter<Long, String> {
    @Override public Class<Long> getSourceType() { return Long.class; }
    @Override public Class<String> getTargetType() { return String.class; }
    @Override public String convert(Long value) { return value == null ? null : value.toString(); }
}