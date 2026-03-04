package org.moper.cap.common.converter.impl;

import org.moper.cap.common.annotation.Priority;

@Priority(100)
public class LongToStringConverter extends DefaultAbstractTypeConverter<Long, String> {
    public LongToStringConverter() {
        super(Long.class, String.class);
    }
    @Override public String convert(Long value) { return value == null ? null : value.toString(); }
}