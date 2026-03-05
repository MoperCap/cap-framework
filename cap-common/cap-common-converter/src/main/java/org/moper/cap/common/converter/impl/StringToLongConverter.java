package org.moper.cap.common.converter.impl;

import org.moper.cap.common.priority.Priority;

@Priority(100)
public class StringToLongConverter extends DefaultAbstractTypeConverter<String, Long> {
    public StringToLongConverter() {
        super(String.class, Long.class);
    }
    @Override public Long convert(String value) { return value == null ? null : Long.parseLong(value.trim()); }
}