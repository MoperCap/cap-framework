package org.moper.cap.common.converter.impl;

public class StringToLongConverter extends DefaultAbstractTypeConverter<String, Long> {
    public StringToLongConverter() {
        super(String.class, Long.class);
    }
    @Override public Long convert(String value) { return value == null ? null : Long.parseLong(value.trim()); }
}