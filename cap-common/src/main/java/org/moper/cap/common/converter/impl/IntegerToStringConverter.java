package org.moper.cap.common.converter.impl;

public class IntegerToStringConverter extends DefaultAbstractTypeConverter<Integer, String> {
    public IntegerToStringConverter() {
        super(Integer.class, String.class);
    }
    @Override public String convert(Integer value) { return value == null ? null : value.toString(); }
}