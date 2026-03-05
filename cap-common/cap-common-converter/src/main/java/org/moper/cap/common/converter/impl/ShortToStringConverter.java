package org.moper.cap.common.converter.impl;

import org.moper.cap.common.priority.Priority;

@Priority(100)
public class ShortToStringConverter extends DefaultAbstractTypeConverter<Short, String> {
    public ShortToStringConverter() {
        super(Short.class, String.class);
    }
    @Override public String convert(Short value) { return value == null ? null : value.toString(); }
}