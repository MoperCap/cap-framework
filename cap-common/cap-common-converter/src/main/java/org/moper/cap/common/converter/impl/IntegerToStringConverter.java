package org.moper.cap.common.converter.impl;

import org.moper.cap.common.priority.Priority;

@Priority(100)
public class IntegerToStringConverter extends DefaultAbstractTypeConverter<Integer, String> {
    public IntegerToStringConverter() {
        super(Integer.class, String.class);
    }
    @Override public String convert(Integer value) { return value == null ? null : value.toString(); }
}