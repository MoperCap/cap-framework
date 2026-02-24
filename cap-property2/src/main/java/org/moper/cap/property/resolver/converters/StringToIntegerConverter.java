package org.moper.cap.property.resolver.converters;

import org.moper.cap.property.resolver.PropertyConverter;

public class StringToIntegerConverter implements PropertyConverter<String, Integer> {
    @Override public Class<String> getSourceType() { return String.class; }
    @Override public Class<Integer> getTargetType() { return Integer.class; }
    @Override public Integer convert(String value) { return value == null ? null : Integer.parseInt(value.trim()); }
}