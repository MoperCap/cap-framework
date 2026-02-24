package org.moper.cap.property.resolver.converters;

import org.moper.cap.property.resolver.PropertyConverter;

public class StringToBooleanConverter implements PropertyConverter<String, Boolean> {
    @Override public Class<String> getSourceType() { return String.class; }
    @Override public Class<Boolean> getTargetType() { return Boolean.class; }
    @Override public Boolean convert(String value) { return value == null ? null : Boolean.parseBoolean(value.trim()); }
}