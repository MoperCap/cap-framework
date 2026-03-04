package org.moper.cap.common.converter.impl;

public class StringToBooleanConverter extends DefaultAbstractTypeConverter<String, Boolean> {
    public StringToBooleanConverter() {
        super(String.class, Boolean.class);
    }
    @Override public Boolean convert(String value) { return value == null ? null : Boolean.parseBoolean(value.trim()); }
}