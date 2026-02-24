package org.moper.cap.property.resolver.converters;

import org.moper.cap.property.resolver.PropertyConverter;

public class BooleanToStringConverter implements PropertyConverter<Boolean, String> {
    @Override public Class<Boolean> getSourceType() { return Boolean.class; }
    @Override public Class<String> getTargetType() { return String.class; }
    @Override public String convert(Boolean value) { return value == null ? null : value.toString(); }
}