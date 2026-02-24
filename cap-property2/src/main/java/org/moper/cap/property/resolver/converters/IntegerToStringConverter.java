package org.moper.cap.property.resolver.converters;

import org.moper.cap.property.resolver.PropertyConverter;

public class IntegerToStringConverter implements PropertyConverter<Integer, String> {
    @Override public Class<Integer> getSourceType() { return Integer.class; }
    @Override public Class<String> getTargetType() { return String.class; }
    @Override public String convert(Integer value) { return value == null ? null : value.toString(); }
}