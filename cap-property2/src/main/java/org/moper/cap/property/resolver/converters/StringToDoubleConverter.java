package org.moper.cap.property.resolver.converters;

public class StringToDoubleConverter extends DefaultAbstractPropertyConverter<String, Double> {
    public StringToDoubleConverter() {
        super(String.class, Double.class);
    }
    @Override public Double convert(String value) { return value == null ? null : Double.parseDouble(value.trim()); }
}