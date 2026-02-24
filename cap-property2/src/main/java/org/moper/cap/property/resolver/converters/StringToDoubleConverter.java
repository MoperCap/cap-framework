package org.moper.cap.property.resolver.converters;

import org.moper.cap.property.resolver.PropertyConverter;

public class StringToDoubleConverter implements PropertyConverter<String, Double> {
    @Override public Class<String> getSourceType() { return String.class; }
    @Override public Class<Double> getTargetType() { return Double.class; }
    @Override public Double convert(String value) { return value == null ? null : Double.parseDouble(value.trim()); }
}