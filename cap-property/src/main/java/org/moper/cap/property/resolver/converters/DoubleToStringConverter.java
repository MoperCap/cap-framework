package org.moper.cap.property.resolver.converters;

public class DoubleToStringConverter extends DefaultAbstractPropertyConverter<Double, String> {
    public DoubleToStringConverter() {
        super(Double.class, String.class);
    }
    @Override public String convert(Double value) { return value == null ? null : value.toString(); }
}