package org.moper.cap.common.converter.impl;

public class StringToIntegerConverter extends DefaultAbstractTypeConverter<String, Integer> {
    public StringToIntegerConverter() {
        super(String.class, Integer.class);
    }
    @Override public Integer convert(String value) { return value == null ? null : Integer.parseInt(value.trim()); }
}