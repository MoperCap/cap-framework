package org.moper.cap.common.converter.impl;

public class DoubleToStringConverter extends DefaultAbstractTypeConverter<Double, String> {
    public DoubleToStringConverter() {
        super(Double.class, String.class);
    }
    @Override public String convert(Double value) { return value == null ? null : value.toString(); }
}