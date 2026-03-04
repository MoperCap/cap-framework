package org.moper.cap.common.converter.impl;

import org.moper.cap.common.annotation.Priority;

@Priority(100)
public class StringToIntegerConverter extends DefaultAbstractTypeConverter<String, Integer> {
    public StringToIntegerConverter() {
        super(String.class, Integer.class);
    }
    @Override public Integer convert(String value) { return value == null ? null : Integer.parseInt(value.trim()); }
}