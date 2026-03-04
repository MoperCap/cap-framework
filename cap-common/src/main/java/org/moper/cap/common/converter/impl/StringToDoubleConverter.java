package org.moper.cap.common.converter.impl;

public class StringToDoubleConverter extends DefaultAbstractTypeConverter<String, Double> {
    public StringToDoubleConverter() {
        super(String.class, Double.class);
    }
    @Override public Double convert(String value) { return value == null ? null : Double.parseDouble(value.trim()); }
}