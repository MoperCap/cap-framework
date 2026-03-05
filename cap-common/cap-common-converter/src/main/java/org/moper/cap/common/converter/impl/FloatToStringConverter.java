package org.moper.cap.common.converter.impl;

import org.moper.cap.common.priority.Priority;

@Priority(100)
public class FloatToStringConverter extends DefaultAbstractTypeConverter<Float, String> {
    public FloatToStringConverter() {
        super(Float.class, String.class);
    }
    @Override public String convert(Float value) { return value == null ? null : value.toString(); }
}