package org.moper.cap.property.resolver.converters;

import org.moper.cap.property.resolver.PropertyConverter;

public class StringToFloatConverter implements PropertyConverter<String, Float> {
    @Override public Class<String> getSourceType() { return String.class; }
    @Override public Class<Float> getTargetType() { return Float.class; }
    @Override public Float convert(String value) { return value == null ? null : Float.parseFloat(value.trim()); }
}