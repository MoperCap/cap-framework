package org.moper.cap.common.converter.impl;

import org.moper.cap.common.priority.Priority;

@Priority(100)
public class BooleanToStringConverter extends DefaultAbstractTypeConverter<Boolean, String> {

    public BooleanToStringConverter() {
        super(Boolean.class, String.class);
    }

    @Override public String convert(Boolean value) { return value == null ? null : value.toString(); }
}