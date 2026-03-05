package org.moper.cap.common.converter.impl;

import org.moper.cap.common.priority.Priority;

@Priority(100)
public class StringToFloatConverter extends DefaultAbstractTypeConverter<String, Float> {
    public StringToFloatConverter() {
        super(String.class, Float.class);
    }
    @Override public Float convert(String value) { return value == null ? null : Float.parseFloat(value.trim()); }
}