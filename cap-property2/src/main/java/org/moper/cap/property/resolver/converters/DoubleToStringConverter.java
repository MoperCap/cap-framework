package org.moper.cap.property.resolver.converters;

import org.moper.cap.property.resolver.PropertyConverter;

public class DoubleToStringConverter implements PropertyConverter<Double, String> {
    @Override public Class<Double> getSourceType() { return Double.class; }
    @Override public Class<String> getTargetType() { return String.class; }
    @Override public String convert(Double value) { return value == null ? null : value.toString(); }
}