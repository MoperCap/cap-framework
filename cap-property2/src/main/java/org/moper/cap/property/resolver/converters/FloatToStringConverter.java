package org.moper.cap.property.resolver.converters;

import org.moper.cap.property.resolver.PropertyConverter;

public class FloatToStringConverter implements PropertyConverter<Float, String> {
    @Override public Class<Float> getSourceType() { return Float.class; }
    @Override public Class<String> getTargetType() { return String.class; }
    @Override public String convert(Float value) { return value == null ? null : value.toString(); }
}